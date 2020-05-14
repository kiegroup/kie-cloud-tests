/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.openshift.scenario;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SimpleWaiter;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.PrometheusDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.PrometheusDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerScenarioImpl extends KieCommonScenario<WorkbenchKieServerScenario> implements WorkbenchKieServerScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private boolean deployPrometheus;
    private PrometheusDeployment prometheusDeployment;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerScenarioImpl.class);

    public WorkbenchKieServerScenarioImpl(Map<String, String> envVariables, boolean deployPrometheus) {
        super(envVariables);
        this.deployPrometheus = deployPrometheus;
    }

    @Override
    protected void deployKieDeployments() {
        logger.info("Processing template and creating resources from " + OpenShiftTemplate.WORKBENCH_KIE_SERVER.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.WORKBENCH_KIE_SERVER.getTemplateUrl(), envVariables);

        workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getAppUser());
        workbenchDeployment.setPassword(envVariables.get(OpenShiftTemplateConstants.DEFAULT_PASSWORD));

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(envVariables.get(OpenShiftTemplateConstants.DEFAULT_PASSWORD));

        if (deployPrometheus) {
            prometheusDeployment = PrometheusDeployer.deploy(project, kieServerDeployment);
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Kie server to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchDeployment, 1);

        logNodeNameOfAllInstances();

        // Used to track persistent volume content due to issues with volume cleanup
        storeProjectInfoToPersistentVolume(workbenchDeployment, "/opt/eap/standalone/data/kie");
    }

    @Override
    public WorkbenchDeployment getWorkbenchDeployment() {
        return workbenchDeployment;
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchDeployment, kieServerDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private void storeProjectInfoToPersistentVolume(Deployment deployment, String persistentVolumeMountPath) {
        String storeCommand = "echo \"Project " + projectName + ", time " + Instant.now() + "\" > " + persistentVolumeMountPath + "/info.txt";
        workbenchDeployment.getInstances().get(0).runCommand("/bin/bash", "-c", storeCommand);
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public Optional<PrometheusDeployment> getPrometheusDeployment() {
        return Optional.ofNullable(prometheusDeployment);
    }

    @Override
    public void changeUsernameAndPassword(String username, String password) {
        if(getDeployments().stream().allMatch(Deployment::isReady)) {
            deploySecretAppUser(username,password);
            logger.info("Restart the environment to update Workbench deployment.");
            getDeployments().parallelStream().forEach(this::scaleToZeroAndBackToReplicas); // if parallel stream make mess because of common fork-join pool use normal stream and adjust scaling (scale all deployments to zero at the same time)
        } else{
            throw new RuntimeException("Application is not ready for Username and password change. Please check first that application is ready.");
        }

    }

    private void deploySecretAppUser(String user, String password) {
        logger.info("Delete old secret '{}'", DeploymentConstants.getAppCredentialsSecretName());
        project.getOpenShift().secrets().withName(DeploymentConstants.getAppCredentialsSecretName()).delete();
        new SimpleWaiter(() -> project.getOpenShift().getSecret(DeploymentConstants.getAppCredentialsSecretName()) == null).timeout(TimeUnit.MINUTES, 2)
                                                                                                                   .reason("Waiting for old secret to be deleted.")
                                                                                                                   .waitFor();

        logger.info("Creating user secret '{}'", DeploymentConstants.getAppCredentialsSecretName());
        Map<String, String> data = new HashMap<>();
        data.put(OpenShiftConstants.KIE_ADMIN_USER, user);
        data.put(OpenShiftConstants.KIE_ADMIN_PWD, password);
        
        project.createSecret(DeploymentConstants.getAppCredentialsSecretName(), data);
        new SimpleWaiter(() -> project.getOpenShift().getSecret(DeploymentConstants.getAppCredentialsSecretName()) != null).timeout(TimeUnit.MINUTES, 2)
                                                                                                                   .reason("Waiting for new secret to be created.")
                                                                                                                   .waitFor();
    }

    private void scaleToZeroAndBackToReplicas(Deployment deployment) {
        int replicas = deployment.getInstances().size();
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(replicas);
        deployment.waitForScale();
    }
}

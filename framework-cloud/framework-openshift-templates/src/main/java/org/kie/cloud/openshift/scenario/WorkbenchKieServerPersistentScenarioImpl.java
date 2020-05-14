/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SimpleWaiter;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.template.ProjectProfile;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerPersistentScenarioImpl extends KieCommonScenario<WorkbenchKieServerScenario> implements WorkbenchKieServerPersistentScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SsoDeployment ssoDeployment;

    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
    private boolean deploySso;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentScenarioImpl.class);

    public WorkbenchKieServerPersistentScenarioImpl(Map<String, String> envVariables, boolean deploySso) {
        super(envVariables);
        this.deploySso = deploySso;
    }

    @Override
    protected void deployKieDeployments() {
        if (deploySso) {
            ssoDeployment = SsoDeployer.deploy(project);

            envVariables.put(OpenShiftTemplateConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

            ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
            envVariables.put(propertyNames.workbenchSsoClient(), projectProfile.getWorkbenchName() + "-client");
            envVariables.put(propertyNames.workbenchSsoSecret(), projectProfile.getWorkbenchName() + "-secret");

            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");
        }

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl(), envVariables);

        workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getAppUser());
        workbenchDeployment.setPassword(DeploymentConstants.getAppPassword());

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(DeploymentConstants.getAppPassword());

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
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchDeployment, kieServerDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private void storeProjectInfoToPersistentVolume(Deployment deployment, String persistentVolumeMountPath) {
        String storeCommand = "echo \"Project " + projectName + ", time " + Instant.now() + "\" > " + persistentVolumeMountPath + "/info.txt";
        workbenchDeployment.getInstances().get(0).runCommand("/bin/bash", "-c", storeCommand);
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Collections.singletonList(workbenchDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Collections.singletonList(kieServerDeployment);
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
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
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

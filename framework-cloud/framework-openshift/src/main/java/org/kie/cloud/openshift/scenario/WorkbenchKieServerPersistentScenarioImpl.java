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
import java.util.List;
import java.util.Map;
import org.kie.cloud.api.deployment.ControllerDeployment;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SSODeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.SSODeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerPersistentScenarioImpl extends OpenShiftScenario implements WorkbenchKieServerPersistentScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SSODeployment ssoDeployment;

    private Map<String, String> envVariables;
    private boolean deploySSO;
    private Map<String, String> ssoEnvVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentScenarioImpl.class);

    public WorkbenchKieServerPersistentScenarioImpl(Map<String, String> envVariables, boolean deploySSO, Map<String, String> ssoEnvVariables) {
        this.envVariables = envVariables;
        this.deploySSO = deploySSO;
        this.ssoEnvVariables = ssoEnvVariables;
    }

    @Override
    public void deploy() {
        super.deploy();

        if (deploySSO) {
            ssoDeployment = SSODeployer.deploy(project, envVariables, ssoEnvVariables);

            envVariables.put(OpenShiftTemplateConstants.SSO_URL, SSODeployer.createSSOEnvVariable(ssoDeployment.getUrl().toString()));
            envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.gettSSORealm());
            envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_CLIENT, "business-central-client");
            envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_SECRET, "business-central-secret");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");
        }

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl(), envVariables);

        workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Kie server to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchDeployment, 1);

        logNodeNameOfAllInstances();

        // Used to track persistent volume content due to issues with volume cleanup
        storeProjectInfoToPersistentVolume(workbenchDeployment, "/opt/eap/standalone/data/bpmsuite");
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
    public SSODeployment getSSODeployment() {
        return ssoDeployment;
    }
}

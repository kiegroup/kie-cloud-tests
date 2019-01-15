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
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.ApbImageGetter;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerPersistentScenarioApb extends OpenShiftScenario implements WorkbenchKieServerPersistentScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SsoDeployment ssoDeployment;

    private Map<String, String> extraVars;
    private boolean deploySso;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentScenarioApb.class);

    public WorkbenchKieServerPersistentScenarioApb(Map<String, String> extraVars, boolean deploySso) {
        this.extraVars = extraVars;
        this.deploySso = deploySso;
    }

    @Override
    public void deploy() {
        super.deploy();

        if (deploySso) {
            ssoDeployment = SsoDeployer.deploy(project);

            extraVars.put(OpenShiftApbConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            extraVars.put(OpenShiftApbConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

            extraVars.put(OpenShiftApbConstants.BUSINESS_CENTRAL_SSO_CLIENT, "business-central-client");
            extraVars.put(OpenShiftApbConstants.BUSINESS_CENTRAL_SSO_SECRET, "business-central-secret");
            extraVars.put(OpenShiftApbConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            extraVars.put(OpenShiftApbConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");

            extraVars.put(OpenShiftApbConstants.SSO_PRINCIPAL_ATTRIBUTE, "preferred_username");
        }

        logger.info("Creating trusted secret");
        deployCustomTrustedSecret();

        logger.info("Processesin APB image plan: " + extraVars.get(OpenShiftApbConstants.APB_PLAN_ID));
        extraVars.put(OpenShiftApbConstants.IMAGE_STREAM_NAMESPACE, projectName);
        extraVars.put("namespace", projectName);
        extraVars.put("cluster", "openshift");
        project.processApbRun(ApbImageGetter.fromImageStream(), extraVars);

        workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setServiceSuffix("-0");
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
        storeProjectInfoToPersistentVolume(workbenchDeployment, "/opt/eap/standalone/data/kie");
    }

    private void storeProjectInfoToPersistentVolume(Deployment deployment, String persistentVolumeMountPath) {
        String storeCommand = "echo \"Project " + projectName + ", time " + Instant.now() + "\" > " + persistentVolumeMountPath + "/info.txt";
        workbenchDeployment.getInstances().get(0).runCommand("/bin/bash", "-c", storeCommand);
    }

    private void deployCustomTrustedSecret() {
        project.processTemplateAndCreateResources(OpenShiftTemplate.CUSTOM_TRUSTED_SECRET.getTemplateUrl(),
                Collections.emptyMap());

        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_SECRET_NAME,
                DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_KEYSTORE_ALIAS,
                DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.BUSINESSCENTRAL_KEYSTORE_PWD,
                DeploymentConstants.getCustomTrustedKeystorePwd());
        extraVars.put(OpenShiftApbConstants.KIESERVER_SECRET_NAME, DeploymentConstants.getCustomTrustedSecretName());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_ALIAS,
                DeploymentConstants.getCustomTrustedKeystoreAlias());
        extraVars.put(OpenShiftApbConstants.KIESERVER_KEYSTORE_PWD, DeploymentConstants.getCustomTrustedKeystorePwd());
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
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
        List<Deployment> deployments = new ArrayList<>(Arrays.asList(workbenchDeployment, kieServerDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
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

}

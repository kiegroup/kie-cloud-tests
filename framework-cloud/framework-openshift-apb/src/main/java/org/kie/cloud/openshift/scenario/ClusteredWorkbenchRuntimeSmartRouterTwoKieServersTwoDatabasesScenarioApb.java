/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.ApbImageGetter;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioApb extends OpenShiftScenario implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario {

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerOneDeployment;
    private KieServerDeploymentImpl kieServerTwoDeployment;
    private DatabaseDeploymentImpl databaseOneDeployment;
    private DatabaseDeploymentImpl databaseTwoDeployment;
    private SsoDeployment ssoDeployment;

    private Map<String, String> extraVars;

    private boolean deploySso;

    private static final Logger logger = LoggerFactory.getLogger(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioApb.class);

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioApb(Map<String, String> extraVars, boolean deploySso) {
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
            // TODO check all Kie Server clients! - because SSO, not all KS are registred to SSO
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

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);

        smartRouterDeployment = createSmartRouterDeployment(project);

        kieServerOneDeployment = createKieServerDeployment(project, "0");
        kieServerTwoDeployment = createKieServerDeployment(project, "1");

        databaseOneDeployment = createDatabaseDeployment(project, "0");
        databaseTwoDeployment = createDatabaseDeployment(project, "1");

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

        logger.info("Waiting for Kie server one deployment to become ready.");
        kieServerOneDeployment.waitForScale();

        logger.info("Waiting for Kie server two deployment to become ready.");
        kieServerTwoDeployment.waitForScale();

        logger.info("Waiting for Database one deployment to become ready.");
        databaseOneDeployment.waitForScale();

        logger.info("Waiting for Database two deployment to become ready.");
        databaseTwoDeployment.waitForScale();

        logger.info("Waiting for Kie servers and Smart router to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchRuntimeDeployment, 3);

        logNodeNameOfAllInstances();
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
    public WorkbenchDeployment getWorkbenchRuntimeDeployment() {
        return workbenchRuntimeDeployment;
    }

    @Override
    public SmartRouterDeployment getSmartRouterDeployment() {
        return smartRouterDeployment;
    }

    @Override
    public KieServerDeployment getKieServerOneDeployment() {
        return kieServerOneDeployment;
    }

    @Override
    public KieServerDeployment getKieServerTwoDeployment() {
        return kieServerTwoDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseOneDeployment() {
        return databaseOneDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseTwoDeployment() {
        return databaseTwoDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerOneDeployment, kieServerTwoDeployment, databaseOneDeployment, databaseTwoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchRuntimeDeploymentImpl workbenchRuntimeDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        return workbenchRuntimeDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl(project);

        return smartRouterDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, String kieServerIndex) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceSuffix("-" + kieServerIndex);

        return kieServerDeployment;
    }

    private DatabaseDeploymentImpl createDatabaseDeployment(Project project, String databaseIndex) {
        DatabaseDeploymentImpl databaseDeployment = new DatabaseDeploymentImpl(project);
        databaseDeployment.setServiceSuffix("-" + databaseIndex);
        return databaseDeployment;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchRuntimeDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerOneDeployment, kieServerTwoDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Arrays.asList(smartRouterDeployment);
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        throw new UnsupportedOperationException("Not supported yet.");
	}
}

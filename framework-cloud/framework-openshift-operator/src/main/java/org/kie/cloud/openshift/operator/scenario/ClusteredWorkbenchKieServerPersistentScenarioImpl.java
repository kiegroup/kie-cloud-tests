/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.operator.scenario;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.SupplierWaiter;
import cz.xtf.core.waiting.WaiterException;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;
import org.kie.cloud.openshift.operator.deployment.KieServerOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.WorkbenchOperatorDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Auth;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.Sso;
import org.kie.cloud.openshift.operator.model.components.Upgrades;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.util.Git;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchKieServerPersistentScenarioImpl extends OpenShiftOperatorScenario<ClusteredWorkbenchKieServerPersistentScenario> implements ClusteredWorkbenchKieServerPersistentScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SsoDeployment ssoDeployment;
    private GitProvider gitProvider;
    private final ScenarioRequest request;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public ClusteredWorkbenchKieServerPersistentScenarioImpl(KieApp kieApp, ScenarioRequest request) {
        super(kieApp);
        this.request = request;
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
    protected void deployCustomResource() {
        if (request.isDeploySso()) {
            ssoDeployment = SsoDeployer.deploySecure(project);
            URL ssoSecureUrl = ssoDeployment.getSecureUrl().orElseThrow(() -> new RuntimeException("RH SSO secure URL not found."));

            Sso sso = new Sso();
            sso.setAdminUser(DeploymentConstants.getSsoServiceUser());
            sso.setAdminPassword(DeploymentConstants.getSsoServicePassword());
            sso.setUrl(SsoDeployer.createSsoEnvVariable(ssoSecureUrl.toString()));
            sso.setRealm(DeploymentConstants.getSsoRealm());
            sso.setDisableSSLCertValidation(true);

            Auth auth = new Auth();
            auth.setSso(sso);
            kieApp.getSpec().setAuth(auth);
        }

        if (request.getGitSettings() != null) {
            gitProvider = Git.createProvider(project, request.getGitSettings());
        }

        configureOperatorForUpgrades();

        registerTrustedSecret(kieApp.getSpec().getObjects().getConsole());
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            registerTrustedSecret(server);
        }

        // deploy application
        getKieAppClient().create(kieApp);
        // Wait until the operator reconciliate the KieApp and add there missing informations
        new SupplierWaiter<KieApp>(() -> getKieAppClient().withName(OpenShiftConstants.getKieApplicationName()).get(), kieApp -> kieApp.getStatus() != null).reason("Waiting for reconciliation to initialize all fields.").timeout(TimeUnit.MINUTES,1).waitFor();

        workbenchDeployment = new WorkbenchOperatorDeployment(project, getKieAppClient());
        workbenchDeployment.setUsername(DeploymentConstants.getAppUser());
        workbenchDeployment.setPassword(DeploymentConstants.getAppPassword());

        kieServerDeployment = new KieServerOperatorDeployment(project, getKieAppClient());
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(DeploymentConstants.getAppPassword());

        logger.info("Waiting until all services are created.");
        try {
            new SimpleWaiter(() -> workbenchDeployment.isReady()).reason("Waiting for Workbench service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
            new SimpleWaiter(() -> kieServerDeployment.isReady()).reason("Waiting for Kie server service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
        } catch (WaiterException e) {
            throw new RuntimeException("Timeout while deploying application.", e);
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        upgradeDeploymentViaOperator();

        logNodeNameOfAllInstances();
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchDeployment, kieServerDeployment, ssoDeployment));
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

    @Override
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
    }

    @Override
    public GitProvider getGitProvider() {
        return gitProvider;
    }

    @Override
    protected String overridesVersionTag() {
        if (request.getUpgradeSettings() != null) {
            return OpenShiftOperatorConstants.getKieOperatorUpgradeFromImageTag();
        }

        return null;
    }

    private void configureOperatorForUpgrades() {
        if (request.getUpgradeSettings() != null) {
            Upgrades upgrades = new Upgrades();
            upgrades.setEnabled(true);
            upgrades.setMinor(request.getUpgradeSettings().isMinor());
            kieApp.getSpec().setUpgrades(upgrades);
        }
    }

    private void upgradeDeploymentViaOperator() {
        if (request.getUpgradeSettings() != null) {
            logger.info("Upgrading deployment...");

            int expectedKieServerVersion = kieServerDeployment.getVersion() + 1;
            int expectedWorkbenchVersion = workbenchDeployment.getVersion() + 1;

            upgradeOperatorToLatestVersion();

            kieServerDeployment.waitForVersion(expectedKieServerVersion);
            workbenchDeployment.waitForVersion(expectedWorkbenchVersion);

            logger.info("Deployment upgraded.");
        }
    }

    private void upgradeOperatorToLatestVersion() {
        project.getOpenShift().apps().deployments().withName(OPERATOR_DEPLOYMENT_NAME).edit()
                 .editSpec()
                     .editTemplate()
                         .editSpec()
                             .editFirstContainer()
                                 .withNewImage(getLatestOperatorVersion())
                             .endContainer()
                         .endSpec()
                     .endTemplate()
                 .endSpec()
                 .done();
    }
}

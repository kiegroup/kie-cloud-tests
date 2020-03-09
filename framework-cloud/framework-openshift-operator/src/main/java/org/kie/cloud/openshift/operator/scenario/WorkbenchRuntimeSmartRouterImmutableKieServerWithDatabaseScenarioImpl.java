/*
 * Copyright 2019 JBoss by Red Hat.
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
package org.kie.cloud.openshift.operator.scenario;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.SupplierWaiter;
import cz.xtf.core.waiting.WaiterException;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.operator.deployment.KieServerOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.SmartRouterOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.WorkbenchRuntimeOperatorDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Auth;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.Sso;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.util.Git;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioImpl extends OpenShiftOperatorScenario<WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario> implements WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario {

    private WorkbenchRuntimeOperatorDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;
    private SsoDeployment ssoDeployment;
    private GitProvider gitProvider;
    private final ScenarioRequest request;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioImpl.class);

    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioImpl(KieApp kieApp, ScenarioRequest request) {
        super(kieApp);
        this.request = request;
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

        registerTrustedSecret(kieApp.getSpec().getObjects().getConsole());
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            registerTrustedSecret(server);
        }

        // deploy application
        getKieAppClient().create(kieApp);
        // Wait until the operator reconciliate the KieApp and add there missing informations
        new SupplierWaiter<KieApp>(() -> getKieAppClient().withName(OpenShiftConstants.getKieApplicationName()).get(), kieApp -> kieApp.getStatus() != null).reason("Waiting for reconciliation to initialize all fields.").timeout(TimeUnit.MINUTES,1).waitFor();

        workbenchRuntimeDeployment = new WorkbenchRuntimeOperatorDeployment(project, getKieAppClient());
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getAppUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getAppPassword());

        smartRouterDeployment = new SmartRouterOperatorDeployment(project, getKieAppClient());

        kieServerDeployment = new KieServerOperatorDeployment(project, getKieAppClient());
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(DeploymentConstants.getAppPassword());

        databaseDeployment = new DatabaseDeploymentImpl(project);

        logger.info("Waiting until all services are created.");
        try {
            new SimpleWaiter(() -> workbenchRuntimeDeployment.isReady()).reason("Waiting for Workbench service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
            new SimpleWaiter(() -> smartRouterDeployment.isReady()).reason("Waiting for Smart router service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
            new SimpleWaiter(() -> kieServerDeployment.isReady()).reason("Waiting for Kie server service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
            new SimpleWaiter(() -> databaseDeployment.isReady()).reason("Waiting for Database service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
        } catch (WaiterException e) {
            throw new RuntimeException("Timeout while deploying application.", e);
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Database deployment to become ready.");
        databaseDeployment.waitForScale();

        logger.info("Waiting for Kie server and Smart router to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchRuntimeDeployment, 2);

        logNodeNameOfAllInstances();

        // Used to track persistent volume content due to issues with volume cleanup
        storeProjectInfoToPersistentVolume(workbenchRuntimeDeployment, "/opt/kie/data");
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
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseDeployment() {
        return databaseDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerDeployment, databaseDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private void storeProjectInfoToPersistentVolume(Deployment deployment, String persistentVolumeMountPath) {
        String storeCommand = "echo \"Project " + projectName + ", time " + Instant.now() + "\" > " + persistentVolumeMountPath + "/info.txt";
        workbenchRuntimeDeployment.getInstances().get(0).runCommand("/bin/bash", "-c", storeCommand);
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Collections.singletonList(workbenchRuntimeDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Collections.singletonList(kieServerDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.singletonList(smartRouterDeployment);
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
}

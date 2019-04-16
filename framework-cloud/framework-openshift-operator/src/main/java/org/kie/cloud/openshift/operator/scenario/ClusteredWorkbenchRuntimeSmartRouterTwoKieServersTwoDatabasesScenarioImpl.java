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

package org.kie.cloud.openshift.operator.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cz.xtf.wait.SimpleWaiter;
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
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.operator.deployment.KieServerOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.SmartRouterOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.WorkbenchRuntimeOperatorDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Auth;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.model.components.Sso;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl extends OpenShiftOperatorScenario<ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario> implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario {

    private KieApp kieApp;

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerOneDeployment;
    private KieServerDeploymentImpl kieServerTwoDeployment;
    private DatabaseDeploymentImpl databaseOneDeployment;
    private DatabaseDeploymentImpl databaseTwoDeployment;
    private SsoDeployment ssoDeployment;
    private boolean deploySso;

    private static final Logger logger = LoggerFactory.getLogger(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl.class);

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(KieApp kieApp, boolean deploySso) {
        this.kieApp = kieApp;
        this.deploySso = deploySso;
    }

    @Override
    protected void deployCustomResource() {

        if (deploySso) {
            ssoDeployment = SsoDeployer.deploy(project);

            Sso sso = new Sso();
            sso.setAdminUser(DeploymentConstants.getSsoServiceUser());
            sso.setAdminPassword(DeploymentConstants.getSsoServicePassword());
            sso.setUrl(SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            sso.setRealm(DeploymentConstants.getSsoRealm());

            Auth auth = new Auth();
            auth.setSso(sso);
            kieApp.getSpec().setAuth(auth);
        }

        registerCustomTrustedSecret(kieApp.getSpec().getObjects().getConsole());
        registerCustomTrustedSecret(kieApp.getSpec().getObjects().getSmartRouter());
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            registerCustomTrustedSecret(server);
        }

        // deploy application
        getKieAppClient().create(kieApp);

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);
        smartRouterDeployment = createSmartRouterDeployment(project);
        kieServerOneDeployment = createKieServerDeployment(project);
        kieServerTwoDeployment = createKieServerDeployment(project, "-2");
        databaseOneDeployment = createDatabaseDeployment(project, OpenShiftConstants.getKieApplicationName() + "-kieserver-postgresql");
        databaseTwoDeployment = createDatabaseDeployment(project, OpenShiftConstants.getKieApplicationName() + "-kieserver-2-postgresql");

        logger.info("Waiting until all services are created.");
        try {
            new SimpleWaiter(() -> workbenchRuntimeDeployment.isReady()).reason("Waiting for Workbench runtime service to be created.").timeout(TimeUnit.MINUTES, 1).execute();
            new SimpleWaiter(() -> smartRouterDeployment.isReady()).reason("Waiting for Smart router service to be created.").timeout(TimeUnit.MINUTES, 1).execute();
            new SimpleWaiter(() -> kieServerOneDeployment.isReady()).reason("Waiting for Kie server one service to be created.").timeout(TimeUnit.MINUTES, 1).execute();
            new SimpleWaiter(() -> kieServerTwoDeployment.isReady()).reason("Waiting for Kie server two service to be created.").timeout(TimeUnit.MINUTES, 1).execute();
            new SimpleWaiter(() -> databaseOneDeployment.isReady()).reason("Waiting for Database one service to be created.").timeout(TimeUnit.MINUTES, 1).execute();
            new SimpleWaiter(() -> databaseTwoDeployment.isReady()).reason("Waiting for Database two service to be created.").timeout(TimeUnit.MINUTES, 1).execute();
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout while deploying application.", e);
        }

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
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
	}

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerOneDeployment, kieServerTwoDeployment, databaseOneDeployment, databaseTwoDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchRuntimeDeploymentImpl workbenchRuntimeDeployment = new WorkbenchRuntimeOperatorDeployment(project, getKieAppClient());
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());
        return workbenchRuntimeDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterOperatorDeployment(project, getKieAppClient());
        return smartRouterDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerOperatorDeployment(project, getKieAppClient());
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        return kieServerDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, String serviceSuffix) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerOperatorDeployment(project, getKieAppClient());
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceSuffix(serviceSuffix);
        return kieServerDeployment;
    }

    private DatabaseDeploymentImpl createDatabaseDeployment(Project project, String serviceName) {
        DatabaseDeploymentImpl databaseDeployment = new DatabaseDeploymentImpl(project);
        databaseDeployment.setServiceName(serviceName);
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
}

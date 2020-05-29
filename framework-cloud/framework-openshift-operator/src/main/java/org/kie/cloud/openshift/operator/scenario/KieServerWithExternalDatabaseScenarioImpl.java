/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.WaiterException;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.DockerDeployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.operator.database.external.OperatorExternalDatabase;
import org.kie.cloud.openshift.operator.database.external.OperatorExternalDatabaseProvider;
import org.kie.cloud.openshift.operator.deployment.KieServerOperatorDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Build;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.util.CustomDatabaseImageBuilder;
import org.kie.cloud.openshift.util.DockerRegistryDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerWithExternalDatabaseScenarioImpl extends OpenShiftOperatorScenario<KieServerWithExternalDatabaseScenario> implements KieServerWithExternalDatabaseScenario {

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenarioImpl.class);

    private KieServerDeploymentImpl kieServerDeployment;
    private DockerDeployment dockerDeployment;

    public KieServerWithExternalDatabaseScenarioImpl(KieApp kieApp) {
        super(kieApp);
    }

    @Override
    protected void deployCustomResource() {
        OperatorExternalDatabase externalDatabase = OperatorExternalDatabaseProvider.getExternalDatabase();

        dockerDeployment = DockerRegistryDeployer.deploy(project);

        String extensionImage = CustomDatabaseImageBuilder.build(project, dockerDeployment, externalDatabase.getExternalDriver());

        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            if (server.getBuild() == null) {
                server.setBuild(new Build());
            }

            server.getBuild().setExtensionImageStreamTag(extensionImage);
            server.getBuild().setExtensionImageStreamTagNamespace(project.getName());
            server.setDatabase(externalDatabase.getDatabaseModel());
            registerTrustedSecret(server);
        }

        // deploy application
        getKieAppClient().create(kieApp);

        kieServerDeployment = new KieServerOperatorDeployment(project, getKieAppClient());
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(DeploymentConstants.getAppPassword());

        logger.info("Waiting until all services are created.");
        try {
            new SimpleWaiter(() -> kieServerDeployment.isReady()).reason("Waiting for Kie server service to be created.").timeout(TimeUnit.MINUTES, 1).waitFor();
        } catch (WaiterException e) {
            throw new RuntimeException("Timeout while deploying application.", e);
        }

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logNodeNameOfAllInstances();
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>(Arrays.asList(kieServerDeployment, dockerDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Collections.emptyList();
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

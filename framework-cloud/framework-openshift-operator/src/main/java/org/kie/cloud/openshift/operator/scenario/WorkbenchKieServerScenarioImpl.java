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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SimpleWaiter;
import cz.xtf.core.waiting.SupplierWaiter;
import cz.xtf.core.waiting.WaiterException;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.PrometheusDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.operator.deployment.KieServerOperatorDeployment;
import org.kie.cloud.openshift.operator.deployment.WorkbenchOperatorDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.util.Git;
import org.kie.cloud.openshift.util.PrometheusDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerScenarioImpl extends OpenShiftOperatorScenario<WorkbenchKieServerScenario> implements WorkbenchKieServerScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private final ScenarioRequest request;
    private PrometheusDeployment prometheusDeployment;
    private GitProvider gitProvider;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerScenarioImpl.class);

    public WorkbenchKieServerScenarioImpl(KieApp kieApp, ScenarioRequest request) {
        super(kieApp);
        this.request = request;
    }

    @Override
    protected void deployCustomResource() {
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

        if (request.isDeployPrometheus()) {
            prometheusDeployment = PrometheusDeployer.deploy(project, kieServerDeployment);
        }

        if (request.getGitSettings() != null) {
            gitProvider = Git.createProvider(project, request.getGitSettings());
        }

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

        logNodeNameOfAllInstances();

        // Used to track persistent volume content due to issues with volume cleanup
        storeProjectInfoToPersistentVolume(workbenchDeployment, "/opt/eap/standalone/data/kie");
    }

    @Override
    public void undeploy() {
        PrometheusDeployer.undeployOperator(project);
        super.undeploy();
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
    public GitProvider getGitProvider() {
        return gitProvider;
    }
}

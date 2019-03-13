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
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioImpl extends KieCommonScenario<ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario> implements ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario {

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;

    private static final Logger logger = LoggerFactory.getLogger(ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioImpl.class);

    public ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioImpl(Map<String, String> envVariables) {
        super(envVariables);
    }

    @Override
    protected void deployKieDeployments() {
        logger.info("Processing template and creating resources from " + OpenShiftTemplate.CLUSTERED_CONSOLE_CLUSTERED_KIE_SERVER_DATABASE.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        envVariables.put(OpenShiftTemplateConstants.POSTGRESQL_IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.CLUSTERED_CONSOLE_CLUSTERED_KIE_SERVER_DATABASE.getTemplateUrl(), envVariables);

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);
        kieServerDeployment = createKieServerDeployment(project, "1");
        databaseDeployment = createDatabaseDeployment(project, "1");

        logger.info("Waiting for Database deployment to become ready.");
        databaseDeployment.waitForScale();

        // TODO: Workaround for KIECLOUD-48, respin Kie server when database is ready
        kieServerDeployment.deleteInstances(kieServerDeployment.getInstances());

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logNodeNameOfAllInstances();
    }

    @Override
    public WorkbenchDeployment getWorkbenchRuntimeDeployment() {
        return workbenchRuntimeDeployment;
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
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchRuntimeDeployment, kieServerDeployment, databaseDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchRuntimeDeploymentImpl workbenchRuntimeDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        return workbenchRuntimeDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, String kieServerSuffix) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceSuffix("-" + kieServerSuffix);

        return kieServerDeployment;
    }

    private DatabaseDeploymentImpl createDatabaseDeployment(Project project, String databaseSuffix) {
        DatabaseDeploymentImpl databaseDeployment = new DatabaseDeploymentImpl(project);
        databaseDeployment.setServiceSuffix("-" + databaseSuffix);
        return databaseDeployment;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchRuntimeDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerDeployment);
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        return null;
	}
}

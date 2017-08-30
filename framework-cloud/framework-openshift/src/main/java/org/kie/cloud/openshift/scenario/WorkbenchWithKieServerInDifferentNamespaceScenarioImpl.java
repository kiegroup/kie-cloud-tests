/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchWithKieServerInDifferentNamespaceScenarioImpl implements WorkbenchWithKieServerScenario {

    private OpenShiftController openshiftController;
    private String workbenchProjectName;
    private String kieServerProjectName;
    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;

    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchWithKieServerInDifferentNamespaceScenarioImpl.class);

    public WorkbenchWithKieServerInDifferentNamespaceScenarioImpl(OpenShiftController openShiftController, Map<String, String> envVariables) {
        this.openshiftController = openShiftController;
        this.envVariables = envVariables;
    }

    @Override
    public String getNamespace() {
        // Default namespace is Workbench's one
        return workbenchProjectName;
    }

    @Override
    public void deploy() {
        // OpenShift restriction: Hostname must be shorter than 63 characters
        workbenchProjectName = UUID.randomUUID().toString().substring(0, 4);
        OpenShiftConstants.getNamespacePrefix().ifPresent(p -> workbenchProjectName = p + "-" + workbenchProjectName);

        logger.info("Generated Workbench project name is " + workbenchProjectName);

        kieServerProjectName = UUID.randomUUID().toString().substring(0, 4);
        OpenShiftConstants.getNamespacePrefix().ifPresent(p -> kieServerProjectName = p + "-" + kieServerProjectName);

        logger.info("Generated Kie server project name is " + kieServerProjectName);

        logger.info("Creating project " + workbenchProjectName);
        Project workbenchProject = openshiftController.createProject(workbenchProjectName);

        logger.info("Creating project " + kieServerProjectName);
        Project kieServerProject = openshiftController.createProject(kieServerProjectName);

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        workbenchProject.createResources(OpenShiftConstants.getKieImageStreams());
        kieServerProject.createResources(OpenShiftConstants.getKieImageStreams());

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplateWorkbench());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, workbenchProjectName);
        workbenchProject.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplateWorkbench(), envVariables);

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplateKieServerDatabase());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, kieServerProjectName);
        kieServerProject.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplateKieServerDatabase(), envVariables);

        workbenchDeployment = createWorkbenchDeployment(workbenchProject, workbenchProjectName);
        kieServerDeployment = createKieServerDeployment(kieServerProject, kieServerProjectName);
        databaseDeployment = createDatabaseDeployment(kieServerProjectName);

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Kie server to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchDeployment);

        logger.info("Waiting for Database deployment to become ready.");
        databaseDeployment.waitForScale();
    }

    @Override
    public void undeploy() {
        InstanceLogUtil.writeDeploymentLogs(this);

        for(Deployment deployment : getDeployments()) {
            if(deployment != null) {
                deployment.scale(0);
                deployment.waitForScale();
            }
        }

        Project workbenchProject = openshiftController.getProject(workbenchProjectName);
        workbenchProject.delete();
        Project kieServerProject = openshiftController.getProject(kieServerProjectName);
        kieServerProject.delete();
    }

    public OpenShiftController getOpenshiftController() {
        return openshiftController;
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
    public DatabaseDeployment getDatabaseDeployment() {
        return databaseDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        return Arrays.asList(workbenchDeployment, kieServerDeployment, databaseDeployment);
    }

    private WorkbenchDeploymentImpl createWorkbenchDeployment(Project workbenchProject, String namespace) {
        WorkbenchDeploymentImpl workbenchDeployment = new WorkbenchDeploymentImpl();
        workbenchDeployment.setOpenShiftController(openshiftController);
        workbenchDeployment.setNamespace(namespace);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());
        workbenchDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        String routeHostWorkbench = workbenchProject.getService(workbenchDeployment.getServiceName()).getRoute().getRouteHost();
        String urlWorkbench = "http://" + routeHostWorkbench;
        try {
            workbenchDeployment.setUrl(new URL(urlWorkbench));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for workbench", e);
        }

        return workbenchDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project kieServerProject, String namespace) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(namespace);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        String routeHostKieServer = kieServerProject.getService(kieServerDeployment.getServiceName()).getRoute().getRouteHost();
        String urlKieServer = "http://" + routeHostKieServer;
        try {
            kieServerDeployment.setUrl(new URL(urlKieServer));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for kie server", e);
        }

        return kieServerDeployment;
    }

    private DatabaseDeploymentImpl createDatabaseDeployment(String namespace) {
        DatabaseDeploymentImpl databaseDeployment = new DatabaseDeploymentImpl();
        databaseDeployment.setOpenShiftController(openshiftController);
        databaseDeployment.setNamespace(namespace);
        databaseDeployment.setApplicationName(OpenShiftConstants.getKieApplicationName());
        return databaseDeployment;
    }
}

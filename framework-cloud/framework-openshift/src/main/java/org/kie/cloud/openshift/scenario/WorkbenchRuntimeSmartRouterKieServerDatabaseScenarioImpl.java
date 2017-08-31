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
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl implements WorkbenchRuntimeSmartRouterKieServerDatabaseScenario {

    private OpenShiftController openshiftController;
    private String projectName;
    private WorkbenchDeploymentImpl workbenchDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;

    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl.class);

    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl(OpenShiftController openShiftController, Map<String, String> envVariables) {
        this.openshiftController = openShiftController;
        this.envVariables = envVariables;
    }

    @Override
    public String getNamespace() {
        return projectName;
    }

    @Override
    public void deploy() {
        // OpenShift restriction: Hostname must be shorter than 63 characters
        projectName = UUID.randomUUID().toString().substring(0, 4);
        OpenShiftConstants.getNamespacePrefix().ifPresent(p -> projectName = p + "-" + projectName);

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        Project project = openshiftController.createProject(projectName);

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplateConsoleSmartRouter());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplateConsoleSmartRouter(), envVariables);

        workbenchDeployment = createWorkbenchDeployment(project, projectName);
        smartRouterDeployment = createSmartRouterDeployment(project, projectName);

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplateKieServerDatabase());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, workbenchDeployment.getUrl().getHost());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, Integer.toString(workbenchDeployment.getUrl().getPort()));
        project.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplateKieServerDatabase(), envVariables);

        kieServerDeployment = createKieServerDeployment(project, projectName);
        databaseDeployment = createDatabaseDeployment(projectName);

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

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

        Project project = openshiftController.getProject(projectName);
        project.delete();
    }

    public OpenShiftController getOpenshiftController() {
        return openshiftController;
    }

    @Override
    public WorkbenchDeployment getWorkbenchDeployment() {
        return workbenchDeployment;
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
        return Arrays.asList(workbenchDeployment, smartRouterDeployment, kieServerDeployment, databaseDeployment);
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

    private SmartRouterDeployment createSmartRouterDeployment(Project smartRouterProject, String namespace) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl();
        smartRouterDeployment.setOpenShiftController(openshiftController);
        smartRouterDeployment.setNamespace(namespace);
        smartRouterDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        String routeHostSmartRouter = smartRouterProject.getService(smartRouterDeployment.getServiceName()).getRoute().getRouteHost();
        String urlSmartRouter = "http://" + routeHostSmartRouter;
        try {
            smartRouterDeployment.setUrl(new URL(urlSmartRouter));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for workbench", e);
        }

        return smartRouterDeployment;
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

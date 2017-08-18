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

public class WorkbenchWithKieServerScenarioImpl implements WorkbenchWithKieServerScenario {

    private OpenShiftController openshiftController;
    private String projectName;
    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;

    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchWithKieServerScenarioImpl.class);

    public WorkbenchWithKieServerScenarioImpl(OpenShiftController openShiftController, Map<String, String> envVariables) {
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

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplate());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplate(), envVariables);

        workbenchDeployment = new WorkbenchDeploymentImpl();
        workbenchDeployment.setOpenShiftController(openshiftController);
        workbenchDeployment.setNamespace(projectName);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());
        workbenchDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());
        workbenchDeployment.setSecureServiceName(OpenShiftConstants.getKieApplicationName());

        String routeHostWorkbench = project.getService(workbenchDeployment.getServiceName()).getRoute().getRouteHost();
        String urlWorkbench = "http://" + routeHostWorkbench;
        try {
            workbenchDeployment.setUrl(new URL(urlWorkbench));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for workbench", e);
        }
        String secureRouteHostWorkbench = project.getService(workbenchDeployment.getSecureServiceName()).getRoute().getRouteHost();
        String secureUrlWorkbench = "https://" + secureRouteHostWorkbench;
        try {
            workbenchDeployment.setSecureUrl(new URL(secureUrlWorkbench));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed secure URL for workbench", e);
        }

        kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(projectName);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());
        kieServerDeployment.setSecureServiceName(OpenShiftConstants.getKieApplicationName());

        String routeHostKieServer = project.getService(kieServerDeployment.getServiceName()).getRoute().getRouteHost();
        String urlKieServer = "http://" + routeHostKieServer;
        try {
            kieServerDeployment.setUrl(new URL(urlKieServer));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL for kie server", e);
        }
        String secureRouteHostKieServer = project.getService(kieServerDeployment.getSecureServiceName()).getRoute().getRouteHost();
        String secureUrlKieServer = "https://" + secureRouteHostKieServer;
        try {
            kieServerDeployment.setSecureUrl(new URL(secureUrlKieServer));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed secure URL for kie server", e);
        }

        databaseDeployment = new DatabaseDeploymentImpl();
        databaseDeployment.setOpenShiftController(openshiftController);
        databaseDeployment.setNamespace(projectName);
        databaseDeployment.setDatabaseName(DeploymentConstants.getDatabaseName());
        databaseDeployment.setApplicationName(OpenShiftConstants.getKieApplicationName());

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
}

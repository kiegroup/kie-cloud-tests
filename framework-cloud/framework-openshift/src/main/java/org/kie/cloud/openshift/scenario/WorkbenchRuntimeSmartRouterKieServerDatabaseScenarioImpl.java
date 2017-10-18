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

import java.util.Arrays;
import java.util.HashMap;
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
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl implements WorkbenchRuntimeSmartRouterKieServerDatabaseScenario {

    private OpenShiftController openshiftController;
    private String projectName;
    private WorkbenchDeployment workbenchRuntimeDeployment;
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

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(projectName);
        smartRouterDeployment = createSmartRouterDeployment(projectName);
        kieServerDeployment = createKieServerDeployment(projectName);
        databaseDeployment = createDatabaseDeployment(projectName);

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.CONSOLE_SMARTROUTER.getTemplateUrl().toString());
        Map<String, String> consoleSmartRouterEnvVariables = new HashMap<String, String>(envVariables);
        consoleSmartRouterEnvVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        consoleSmartRouterEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, workbenchRuntimeDeployment.getUrl().getHost());
        consoleSmartRouterEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, Integer.toString(workbenchRuntimeDeployment.getUrl().getPort()));
        project.processTemplateAndCreateResources(OpenShiftTemplate.CONSOLE_SMARTROUTER.getTemplateUrl(), consoleSmartRouterEnvVariables);

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.KIE_SERVER_S2I.getTemplateUrl().toString());
        Map<String, String> kieServerEnvVariables = new HashMap<String, String>(envVariables);
        kieServerEnvVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOST, kieServerDeployment.getUrl().getHost());
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PORT, Integer.toString(kieServerDeployment.getUrl().getPort()));
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, smartRouterDeployment.getUrl().getHost());
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, Integer.toString(smartRouterDeployment.getUrl().getPort()));
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, workbenchRuntimeDeployment.getUrl().getHost());
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, Integer.toString(workbenchRuntimeDeployment.getUrl().getPort()));
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, workbenchRuntimeDeployment.getUsername());
        kieServerEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, workbenchRuntimeDeployment.getPassword());
        project.processTemplateAndCreateResources(OpenShiftTemplate.KIE_SERVER_S2I.getTemplateUrl(), kieServerEnvVariables);

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Kie server and Smart router to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchRuntimeDeployment, 2);

        // TODO: temporary disabled due to Kie server S2I workaround
//        logger.info("Waiting for Database deployment to become ready.");
//        databaseDeployment.waitForScale();
    }

    @Override
    public void undeploy() {
        InstanceLogUtil.writeDeploymentLogs(this);

        for(Deployment deployment : getDeployments()) {
            if(deployment != null && deployment.isReady()) {
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
        return Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerDeployment, databaseDeployment);
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(String namespace) {
        WorkbenchRuntimeDeploymentImpl workbenchRuntimeDeployment = new WorkbenchRuntimeDeploymentImpl();
        workbenchRuntimeDeployment.setOpenShiftController(openshiftController);
        workbenchRuntimeDeployment.setNamespace(namespace);
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());
        workbenchRuntimeDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return workbenchRuntimeDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(String namespace) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl();
        smartRouterDeployment.setOpenShiftController(openshiftController);
        smartRouterDeployment.setNamespace(namespace);
        smartRouterDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return smartRouterDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(String namespace) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(namespace);
        // TODO: Hardcoded, see WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl
//        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
//        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setUsername("executionUser");
        kieServerDeployment.setPassword("execution1!");
        kieServerDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

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

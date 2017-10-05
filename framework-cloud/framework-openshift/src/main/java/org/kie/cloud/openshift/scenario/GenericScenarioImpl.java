/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.openshift.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ScenarioConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericScenarioImpl implements GenericScenario {

    private OpenShiftController openshiftController;
    private String projectName;
    private WorkbenchDeploymentImpl workbenchDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;

    private Map<String, String> envVariables;
    private Map<ScenarioConstants, String> kieAppTemplates;

    private static final Logger logger = LoggerFactory.getLogger(GenericScenarioImpl.class);

    public GenericScenarioImpl(OpenShiftController openShiftController, Map<String, String> envVariables, Map<ScenarioConstants, String> kieAppTemplates) {
        this.openshiftController = openShiftController;
        this.envVariables = envVariables;
        this.kieAppTemplates = kieAppTemplates;
    }

    @Override
    public WorkbenchDeployment getWorkbenchDeployment() {
        if (isWorkbenchInScenario()) {
            return workbenchDeployment;
        }
        throw new RuntimeException("Workbench or Kie controller deployment is not in this scenario");
    }

    @Override
    public SmartRouterDeployment getSmartRouterDeployment() {
        if (isSmartRouterInScenario()) {
            return smartRouterDeployment;
        }
        throw new RuntimeException("Smart router is not in this scenario");
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        if (isKieServerInScenario()) {
            return kieServerDeployment;
        }
        throw new RuntimeException("Kie server deployment is not in this scenario");
    }

    @Override
    public DatabaseDeployment getDatabaseDeployment() {
        if (isDatabaseInScenario()) {
            return databaseDeployment;
        }
        throw new RuntimeException("Database is not in this scenario");
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

        if (isWorkbenchInScenario()) {
            String appTemplateWorkbench = kieAppTemplates.get(ScenarioConstants.WORKBENCH_TEMPLATE_KEY);

            logger.info("Processing template and creating resources from " + appTemplateWorkbench);
            envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
            project.processTemplateAndCreateResources(appTemplateWorkbench, envVariables);

            workbenchDeployment = createWorkbenchDeployment(projectName);
        }

        if (isKieServerInScenario()) {
            String appTemplateKieServer = kieAppTemplates.get(ScenarioConstants.KIE_SERVER_TEMPLATE_KEY);

            logger.info("Processing template and creating resources from " + appTemplateKieServer);
            envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
            project.processTemplateAndCreateResources(appTemplateKieServer, envVariables);

            kieServerDeployment = createKieServerDeployment(projectName);
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        if (isWorkbenchInScenario()) {
            workbenchDeployment.waitForScale();
        }
        logger.info("Waiting for Kie server deployment to become ready.");
        if (isKieServerInScenario()) {
            kieServerDeployment.waitForScale();
        }
    }

    @Override
    public void undeploy() {
        InstanceLogUtil.writeDeploymentLogs(this);

        for (Deployment deployment : getDeployments()) {
            if (deployment != null) {
                deployment.scale(0);
                deployment.waitForScale();
            }
        }

        Project project = openshiftController.getProject(projectName);
        project.delete();

    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>();
        if (isWorkbenchInScenario()) {
            deployments.add(workbenchDeployment);
        }
        if (isSmartRouterInScenario()) {
            deployments.add(smartRouterDeployment);
        }
        if (isKieServerInScenario()) {
            deployments.add(kieServerDeployment);
        }
        if (isDatabaseInScenario()) {
            deployments.add(databaseDeployment);
        }

        return deployments;
    }

    private boolean isWorkbenchInScenario() {
        return kieAppTemplates.containsKey(ScenarioConstants.WORKBENCH_TEMPLATE_KEY);
    }

    private boolean isSmartRouterInScenario() {
        return kieAppTemplates.containsKey(ScenarioConstants.SMART_ROUTER_TEMPLATE_KEY);
    }

    private boolean isKieServerInScenario() {
        return kieAppTemplates.containsKey(ScenarioConstants.KIE_SERVER_TEMPLATE_KEY);
    }

    private boolean isDatabaseInScenario() {
        return kieAppTemplates.containsKey(ScenarioConstants.DATABASE_TEMPLATE_KEY) && kieAppTemplates.get(ScenarioConstants.DATABASE_TEMPLATE_KEY) != null;
    }

    private KieServerDeploymentImpl createKieServerDeployment(String namespace) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(namespace);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return kieServerDeployment;
    }

    private WorkbenchDeploymentImpl createWorkbenchDeployment(String namespace) {
        WorkbenchDeploymentImpl workbenchDeployment = new WorkbenchDeploymentImpl();
        workbenchDeployment.setOpenShiftController(openshiftController);
        workbenchDeployment.setNamespace(namespace);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());
        workbenchDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return workbenchDeployment;
    }
}

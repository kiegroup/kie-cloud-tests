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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericScenarioImpl implements GenericScenario {

    private OpenShiftController openshiftController;
    private String projectName;

    private List<WorkbenchDeployment> workbenchDeployments;
    private List<KieServerDeployment> kieServerDeployments;
    private List<SmartRouterDeployment> smartRouterDeployments;

    private List<DeploymentSettings> kieServerSettingsList;
    private List<DeploymentSettings> workbenchSettingsList;
    private List<DeploymentSettings> monitoringSettingsList;
    private List<DeploymentSettings> smartRouterSettingsList;

    private static final Logger logger = LoggerFactory.getLogger(GenericScenarioImpl.class);

    public GenericScenarioImpl(OpenShiftController openshiftController, List<DeploymentSettings> kieServerSettingsList, List<DeploymentSettings> workbenchSettingsList, List<DeploymentSettings> monitoringSettingsList, List<DeploymentSettings> smartRouterSettingsList) {
        this.openshiftController = openshiftController;
        this.kieServerSettingsList = kieServerSettingsList;
        this.workbenchSettingsList = workbenchSettingsList;
        this.monitoringSettingsList = monitoringSettingsList;
        this.smartRouterSettingsList = smartRouterSettingsList;

        workbenchDeployments = new ArrayList<>();
        kieServerDeployments = new ArrayList<>();
        smartRouterDeployments = new ArrayList<>();
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return workbenchDeployments;
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return kieServerDeployments;
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return smartRouterDeployments;
    }

    @Override
    public String getNamespace() {
        return projectName;
    }

    @Override
    public void deploy() {
        workbenchDeployments.clear();
        kieServerDeployments.clear();
        smartRouterDeployments.clear();

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

        for (DeploymentSettings workbenchSettings : workbenchSettingsList) {
            deployTemplateWithSettings(project, workbenchSettings);
            workbenchDeployments.add(createWorkbenchDeployment(projectName, workbenchSettings));
        }

        for (DeploymentSettings monitoringSettings : monitoringSettingsList) {
            deployTemplateWithSettings(project, monitoringSettings);
            workbenchDeployments.add(createWorkbenchMonitoringDeployment(projectName, monitoringSettings));
        }

        for (DeploymentSettings smartRouterSettings : smartRouterSettingsList) {
            deployTemplateWithSettings(project, smartRouterSettings);
            smartRouterDeployments.add(createSmartRouterDeployment(projectName, smartRouterSettings));
        }

        for (DeploymentSettings kieServerSettings : kieServerSettingsList) {
            deployTemplateWithSettings(project, kieServerSettings);
            kieServerDeployments.add(createKieServerDeployment(projectName, kieServerSettings));
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        for (WorkbenchDeployment workbenchDeployment : workbenchDeployments) {
            workbenchDeployment.waitForScale();
        }
        logger.info("Waiting for Kie server deployment to become ready.");
        for (KieServerDeployment kieServerDeployment : kieServerDeployments) {
            kieServerDeployment.waitForScale();
        }
    }

    private void deployTemplateWithSettings(Project project, DeploymentSettings deploymentSettings) {
        Map<String, String> envVariables = new HashMap<>(deploymentSettings.getEnvVariables());

        logger.info("Processing template and creating resources from " + deploymentSettings.getDeploymentScriptUrl());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(deploymentSettings.getDeploymentScriptUrl(), envVariables);
    }

    @Override
    public void undeploy() {
        InstanceLogUtil.writeDeploymentLogs(this);

        for (Deployment deployment : getDeployments()) {
            if (deployment != null && deployment.isReady()) {
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
        deployments.addAll(workbenchDeployments);
        deployments.addAll(kieServerDeployments);
        deployments.addAll(smartRouterDeployments);
        return deployments;
    }

    private KieServerDeploymentImpl createKieServerDeployment(String namespace, DeploymentSettings deploymentSettings) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(namespace);
        kieServerDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        kieServerDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword()));

        return kieServerDeployment;
    }

    private WorkbenchDeploymentImpl createWorkbenchDeployment(String namespace, DeploymentSettings deploymentSettings) {
        WorkbenchDeploymentImpl workbenchDeployment = new WorkbenchDeploymentImpl();
        workbenchDeployment.setOpenShiftController(openshiftController);
        workbenchDeployment.setNamespace(namespace);
        workbenchDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        workbenchDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));

        return workbenchDeployment;
    }

    private WorkbenchDeployment createWorkbenchMonitoringDeployment(String namespace, DeploymentSettings deploymentSettings) {
        WorkbenchRuntimeDeploymentImpl monitoringDeployment = new WorkbenchRuntimeDeploymentImpl();
        monitoringDeployment.setOpenShiftController(openshiftController);
        monitoringDeployment.setNamespace(namespace);
        monitoringDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        monitoringDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));
        monitoringDeployment.setServiceName(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.APPLICATION_NAME, OpenShiftConstants.getKieApplicationName()));

        return monitoringDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(String namespace, DeploymentSettings deploymentSettings) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl();
        smartRouterDeployment.setOpenShiftController(openshiftController);
        smartRouterDeployment.setNamespace(namespace);
        smartRouterDeployment.setServiceName(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.APPLICATION_NAME, OpenShiftConstants.getKieApplicationName()));

        return smartRouterDeployment;
    }

}

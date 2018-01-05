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

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericScenarioImpl extends OpenShiftScenario implements GenericScenario {

    private List<WorkbenchDeployment> workbenchDeployments;
    private List<KieServerDeployment> kieServerDeployments;
    private List<SmartRouterDeployment> smartRouterDeployments;

    private List<DeploymentSettings> kieServerSettingsList;
    private List<DeploymentSettings> workbenchSettingsList;
    private List<DeploymentSettings> monitoringSettingsList;
    private List<DeploymentSettings> smartRouterSettingsList;

    private static final Logger logger = LoggerFactory.getLogger(GenericScenarioImpl.class);

    public GenericScenarioImpl(List<DeploymentSettings> kieServerSettingsList, List<DeploymentSettings> workbenchSettingsList, List<DeploymentSettings> monitoringSettingsList, List<DeploymentSettings> smartRouterSettingsList) {
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
        super.deploy();

        workbenchDeployments.clear();
        kieServerDeployments.clear();
        smartRouterDeployments.clear();

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        for (DeploymentSettings workbenchSettings : workbenchSettingsList) {
            deployTemplateWithSettings(project, workbenchSettings);
            workbenchDeployments.add(createWorkbenchDeployment(project, workbenchSettings));
        }

        for (DeploymentSettings monitoringSettings : monitoringSettingsList) {
            deployTemplateWithSettings(project, monitoringSettings);
            workbenchDeployments.add(createWorkbenchMonitoringDeployment(project, monitoringSettings));
        }

        for (DeploymentSettings smartRouterSettings : smartRouterSettingsList) {
            deployTemplateWithSettings(project, smartRouterSettings);
            smartRouterDeployments.add(createSmartRouterDeployment(project, smartRouterSettings));
        }

        for (DeploymentSettings kieServerSettings : kieServerSettingsList) {
            deployTemplateWithSettings(project, kieServerSettings);
            kieServerDeployments.add(createKieServerDeployment(project, kieServerSettings));
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
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>();
        deployments.addAll(workbenchDeployments);
        deployments.addAll(kieServerDeployments);
        deployments.addAll(smartRouterDeployments);
        return deployments;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, DeploymentSettings deploymentSettings) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        kieServerDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword()));

        return kieServerDeployment;
    }

    private WorkbenchDeploymentImpl createWorkbenchDeployment(Project project, DeploymentSettings deploymentSettings) {
        WorkbenchDeploymentImpl workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        workbenchDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));

        return workbenchDeployment;
    }

    private WorkbenchDeployment createWorkbenchMonitoringDeployment(Project project, DeploymentSettings deploymentSettings) {
        WorkbenchRuntimeDeploymentImpl monitoringDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        monitoringDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        monitoringDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));
        monitoringDeployment.setServiceName(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.APPLICATION_NAME, OpenShiftConstants.getKieApplicationName()));

        return monitoringDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project, DeploymentSettings deploymentSettings) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl(project);
        smartRouterDeployment.setServiceName(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.APPLICATION_NAME, OpenShiftConstants.getKieApplicationName()));

        return smartRouterDeployment;
    }

}

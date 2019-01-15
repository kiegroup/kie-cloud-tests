/*
 * Copyright 2018 JBoss by Red Hat.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.ApbImageGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericScenarioApb extends OpenShiftScenario implements GenericScenario {

    private List<WorkbenchDeployment> workbenchDeployments;
    private List<KieServerDeployment> kieServerDeployments;
    private List<SmartRouterDeployment> smartRouterDeployments;
    private List<ControllerDeployment> controllerDeployments;

    private List<DeploymentSettings> kieServerSettingsList;
    private List<DeploymentSettings> workbenchSettingsList;
    private List<DeploymentSettings> monitoringSettingsList;
    private List<DeploymentSettings> smartRouterSettingsList;
    private List<DeploymentSettings> controllerSettingsList;

    private static final Logger logger = LoggerFactory.getLogger(GenericScenarioApb.class);

    public GenericScenarioApb(List<DeploymentSettings> kieServerSettingsList, List<DeploymentSettings> workbenchSettingsList, List<DeploymentSettings> monitoringSettingsList, List<DeploymentSettings> smartRouterSettingsList, List<DeploymentSettings> controllerSettingsList) {
        this.kieServerSettingsList = kieServerSettingsList;
        this.workbenchSettingsList = workbenchSettingsList;
        this.monitoringSettingsList = monitoringSettingsList;
        this.smartRouterSettingsList = smartRouterSettingsList;
        this.controllerSettingsList = controllerSettingsList;

        workbenchDeployments = new ArrayList<>();
        kieServerDeployments = new ArrayList<>();
        smartRouterDeployments = new ArrayList<>();
        controllerDeployments = new ArrayList<>();
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
    public List<ControllerDeployment> getControllerDeployments() {
        return controllerDeployments;
    }

    @Override
    public String getNamespace() {
        return projectName;
    }

    @Override
    public void deploy() {
        super.deploy();

        logger.info("Deploy custom trusted certifcate.");
        project.processTemplateAndCreateResources(OpenShiftTemplate.CUSTOM_TRUSTED_SECRET.getTemplateUrl(), Collections.emptyMap());
        // extra vars for custom secret are set in settings builder

        workbenchDeployments.clear();
        controllerDeployments.clear();
        kieServerDeployments.clear();
        smartRouterDeployments.clear();

        for (DeploymentSettings workbenchSettings : workbenchSettingsList) {
            deployApbWithSettings(project, workbenchSettings);
            workbenchDeployments.add(createWorkbenchDeployment(project, workbenchSettings));
        }

        for (DeploymentSettings monitoringSettings : monitoringSettingsList) {
            deployApbWithSettings(project, monitoringSettings);
            workbenchDeployments.add(createWorkbenchMonitoringDeployment(project, monitoringSettings));

            // In APB is smartrouter added with monitoring...
            smartRouterDeployments.add(createSmartRouterDeployment(project, monitoringSettings));
        }

        for (DeploymentSettings controllerSettings : controllerSettingsList) {
            deployApbWithSettings(project, controllerSettings);
            controllerDeployments.add(createControllerDeployment(project, controllerSettings));
        }

        for (DeploymentSettings smartRouterSettings : smartRouterSettingsList) {
            deployApbWithSettings(project, smartRouterSettings);
            smartRouterDeployments.add(createSmartRouterDeployment(project, smartRouterSettings));
        }

        int kieServerSuffixCounter = 0;
        for (DeploymentSettings kieServerSettings : kieServerSettingsList) {
            deployApbWithSettings(project, kieServerSettings);
            kieServerDeployments.add(createKieServerDeployment(project, kieServerSettings, kieServerSuffixCounter));
            kieServerSuffixCounter++;
        }

        logger.info("Waiting for Workbench deployment to become ready.");
        for (WorkbenchDeployment workbenchDeployment : workbenchDeployments) {
            workbenchDeployment.waitForScale();
        }
        logger.info("Waiting for Controller deployment to become ready.");
        for(ControllerDeployment controllerDeployment : controllerDeployments) {
            controllerDeployment.waitForScale();
        }
        logger.info("Waiting for Kie server deployment to become ready.");
        for (KieServerDeployment kieServerDeployment : kieServerDeployments) {
            kieServerDeployment.waitForScale();
        }

        logNodeNameOfAllInstances();
    }

    private void deployApbWithSettings(Project project, DeploymentSettings deploymentSettings) {
        Map<String, String> extraVars = new HashMap<>(deploymentSettings.getEnvVariables());

        logger.info("Processesin APB image plan: " + extraVars.get(OpenShiftApbConstants.APB_PLAN_ID));
        //extraVars.put(OpenShiftApbConstants.IMAGE_STREAM_NAMESPACE, projectName);
        extraVars.put("namespace", projectName);
        extraVars.put("cluster", "openshift");
        project.processApbRun(ApbImageGetter.fromImageStream(), extraVars);
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>();
        deployments.addAll(workbenchDeployments);
        deployments.addAll(kieServerDeployments);
        deployments.addAll(smartRouterDeployments);
        deployments.addAll(controllerDeployments);
        return deployments;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, DeploymentSettings deploymentSettings, int suffix) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftApbConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        kieServerDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftApbConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword()));
        kieServerDeployment.setServiceSuffix("-" + suffix);

        return kieServerDeployment;
    }

    private WorkbenchDeploymentImpl createWorkbenchDeployment(Project project, DeploymentSettings deploymentSettings) {
        throw new UnsupportedOperationException("Not supported for apb.");
    }

    private WorkbenchDeployment createWorkbenchMonitoringDeployment(Project project, DeploymentSettings deploymentSettings) {
        WorkbenchRuntimeDeploymentImpl monitoringDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        monitoringDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftApbConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        monitoringDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftApbConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));

        return monitoringDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project, DeploymentSettings deploymentSettings) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl(project);

        return smartRouterDeployment;
    }

    private ControllerDeployment createControllerDeployment(Project project, DeploymentSettings deploymentSettings) {
        throw new UnsupportedOperationException("Not supported for apb.");
    }

}

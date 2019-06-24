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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.AmqDeploymentImpl;
import org.kie.cloud.openshift.deployment.ControllerDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.ServiceUtil;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.settings.GenericScenarioSettings;
import org.kie.cloud.openshift.template.ProjectProfile;
import org.kie.cloud.openshift.util.AmqImageStreamDeployer;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericScenarioImpl extends OpenShiftScenario<GenericScenario> implements GenericScenario {

    private List<WorkbenchDeployment> workbenchDeployments;
    private List<KieServerDeployment> kieServerDeployments;
    private List<SmartRouterDeployment> smartRouterDeployments;
    private List<ControllerDeployment> controllerDeployments;
    private SsoDeployment ssoDeployment;
    private AmqDeployment amqDeployment;

    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();

    GenericScenarioSettings scenarioSettings;

    private static final Logger logger = LoggerFactory.getLogger(GenericScenarioImpl.class);

    public GenericScenarioImpl(GenericScenarioSettings scenarioSettings) {
        this.scenarioSettings = scenarioSettings;

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
    protected void deployKieDeployments() {
        logger.info("Creating AMQ secret");
        createAmqSecret();
        logger.info("AMQ secret created");
        logger.info("Creating AMQ image stream");
        AmqImageStreamDeployer.deploy(project);
        logger.info("AMQ image stream created");

        workbenchDeployments.clear();
        controllerDeployments.clear();
        kieServerDeployments.clear();
        smartRouterDeployments.clear();

        if(scenarioSettings.getDeploySso()) {
            ssoDeployment = SsoDeployer.deploy(project);

            scenarioSettings.getAllSettings().stream().forEach((DeploymentSettings deploymentSettings) -> {
                Map<String, String> envVariables = deploymentSettings.getEnvVariables();
                envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
                envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
                envVariables.put(OpenShiftTemplateConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
                envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

                ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
                envVariables.put(propertyNames.workbenchSsoClient(),
                        envVariables.get(OpenShiftTemplateConstants.APPLICATION_NAME) + "-"
                                + projectProfile.getWorkbenchName() + "-client");
                envVariables.put(propertyNames.workbenchSsoSecret(),
                        envVariables.get(OpenShiftTemplateConstants.APPLICATION_NAME) + "-"
                                + projectProfile.getWorkbenchName() + "-secret");

                envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT,
                        envVariables.get(OpenShiftTemplateConstants.APPLICATION_NAME) + "-kie-server-client");
                envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET,
                        envVariables.get(OpenShiftTemplateConstants.APPLICATION_NAME) + "-kie-server-secret");
            });

        }

        if (scenarioSettings.getLdapSettings() != null) {
            scenarioSettings.getAllSettings().stream().forEach((DeploymentSettings deploymentSettings) -> {
                Map<String, String> envVariables = deploymentSettings.getEnvVariables();
                envVariables.putAll(scenarioSettings.getLdapSettings().getEnvVariables());
            });
        }

        for (DeploymentSettings workbenchSettings : scenarioSettings.getWorkbenchSettingsList()) {
            deployTemplateWithSettings(project, workbenchSettings);
            workbenchDeployments.add(createWorkbenchDeployment(project, workbenchSettings));
        }

        for (DeploymentSettings monitoringSettings : scenarioSettings.getMonitoringSettingsList()) {
            deployTemplateWithSettings(project, monitoringSettings);
            workbenchDeployments.add(createWorkbenchMonitoringDeployment(project, monitoringSettings));
        }

        for (DeploymentSettings controllerSettings : scenarioSettings.getControllerSettingsList()) {
            deployTemplateWithSettings(project, controllerSettings);
            controllerDeployments.add(createControllerDeployment(project, controllerSettings));
        }

        for (DeploymentSettings smartRouterSettings : scenarioSettings.getSmartRouterSettingsList()) {
            deployTemplateWithSettings(project, smartRouterSettings);
            smartRouterDeployments.add(createSmartRouterDeployment(project, smartRouterSettings));
        }

        for (DeploymentSettings kieServerSettings : scenarioSettings.getKieServerSettingsList()) {
            deployTemplateWithSettings(project, kieServerSettings);
            kieServerDeployments.add(createKieServerDeployment(project, kieServerSettings));
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

        // check if there is an AMQ deployment
        try {
            logger.info("Searching for AMQ.");
            ServiceUtil.getAmqJolokiaServiceName(project.getOpenShift());
            amqDeployment = createAmqDeployment(project);
            addAmqSecretToAmqServiceAccount();
            logger.info("AMQ found and deployment created.");
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("not found.")) {
                logger.info("AMQ not found, skipping creating AMQ deployment.", e);
            } else {
                logger.error("Runtime exception thrown and AMQ was founded.",e);
                throw new RuntimeException("Exception catched during creating AMQ deployment.",e);
            }
        }

        logNodeNameOfAllInstances();
    }

    private void addAmqSecretToAmqServiceAccount() {
        project.getOpenShift()
            .serviceAccounts()
            .inNamespace(getNamespace())
            .withName("amq-service-account")
                .edit()
                    .addNewSecret()
                        .withName("amq-app-secret")
                    .endSecret()
                .done();
    }
    private void createAmqSecret() {
        try {
            project.getOpenShift().secrets().createOrReplaceWithNew()
            .withNewMetadata()
                .withName("amq-app-secret")
                .withNamespace(getNamespace())
            .endMetadata()
            .addToData("broker.ks", 
                    Base64.encodeBase64String(Files.readAllBytes(Paths.get(DeploymentConstants.getCertificateDir()+"/broker.ks")))) //TODO replace with constants
            .addToData("broker.ts", 
                    Base64.encodeBase64String(Files.readAllBytes(Paths.get(DeploymentConstants.getCertificateDir()+"/broker.ts")))) //TODO replace with constants
            .done();
        } catch (IOException ex) {
            throw new RuntimeException("Exception cat during creating AMQ secret." , ex);
        }
    }


    private void deployTemplateWithSettings(Project project, DeploymentSettings deploymentSettings) {
        Map<String, String> envVariables = new HashMap<>(deploymentSettings.getEnvVariables());

        logger.info("Processing template and creating resources from " + deploymentSettings.getDeploymentScriptUrl());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        envVariables.put(OpenShiftTemplateConstants.POSTGRESQL_IMAGE_STREAM_NAMESPACE, projectName);
        envVariables.put(OpenShiftTemplateConstants.MYSQL_IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(deploymentSettings.getDeploymentScriptUrl(), envVariables);
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>();
        deployments.addAll(workbenchDeployments);
        deployments.addAll(kieServerDeployments);
        deployments.addAll(smartRouterDeployments);
        deployments.addAll(controllerDeployments);
        if (ssoDeployment != null) {
            deployments.add(ssoDeployment);
        }
        if (amqDeployment != null) {
            deployments.add(amqDeployment);
        }
        return deployments;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, DeploymentSettings deploymentSettings) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()));
        kieServerDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword()));
        kieServerDeployment.scale(1);

        return kieServerDeployment;
    }

    private WorkbenchDeploymentImpl createWorkbenchDeployment(Project project, DeploymentSettings deploymentSettings) {
        WorkbenchDeploymentImpl workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        workbenchDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));
        workbenchDeployment.scale(1);

        return workbenchDeployment;
    }

    private WorkbenchDeployment createWorkbenchMonitoringDeployment(Project project, DeploymentSettings deploymentSettings) {
        WorkbenchRuntimeDeploymentImpl monitoringDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        monitoringDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()));
        monitoringDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()));
        monitoringDeployment.scale(1);

        return monitoringDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project, DeploymentSettings deploymentSettings) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl(project);
        smartRouterDeployment.setServiceName(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.APPLICATION_NAME, OpenShiftConstants.getKieApplicationName()));
        smartRouterDeployment.scale(1);

        return smartRouterDeployment;
    }

    private ControllerDeployment createControllerDeployment(Project project, DeploymentSettings deploymentSettings) {
        ControllerDeploymentImpl controllerDeployment = new ControllerDeploymentImpl(project);
        controllerDeployment.setUsername(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser()));
        controllerDeployment.setPassword(deploymentSettings.getEnvVariables().getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword()));
        controllerDeployment.scale(1);

        return controllerDeployment;
    }

    private AmqDeployment createAmqDeployment(Project project) {
        AmqDeploymentImpl amqDeployment = new AmqDeploymentImpl(project);
        amqDeployment.setUsername(DeploymentConstants.getAmqUsername());
        amqDeployment.setPassword(DeploymentConstants.getAmqPassword());

        return amqDeployment;
    }

}

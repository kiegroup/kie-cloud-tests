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

import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.AmqDeploymentImpl;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.AmqImageStreamDeployer;
import org.kie.cloud.openshift.util.AmqSecretDeployer;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioImpl extends KieCommonScenario<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> implements WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario {

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private DatabaseDeploymentImpl databaseDeployment;
    private SsoDeployment ssoDeployment;
    private AmqDeploymentImpl amqDeployment;

    private boolean deploySso;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioImpl(Map<String, String> envVariables, boolean deploySso) {
        super(envVariables);
        this.deploySso = deploySso;
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
    protected void deployKieDeployments() {
        if (deploySso) {
            ssoDeployment = SsoDeployer.deploy(project);

            envVariables.put(OpenShiftTemplateConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");
        }

        logger.info("Creating AMQ secret");
        AmqSecretDeployer.create(project);
        logger.info("AMQ secret created");
        logger.info("Creating AMQ image stream");
        AmqImageStreamDeployer.deploy(project);
        logger.info("AMQ image stream created");

        logger.info("Processing template and creating resources from {}", OpenShiftTemplate.KIE_SERVER_DATABASE_S2I_AMQ.getTemplateUrl());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, project.getName());
        envVariables.put(OpenShiftTemplateConstants.POSTGRESQL_IMAGE_STREAM_NAMESPACE, project.getName());
        project.processTemplateAndCreateResources(OpenShiftTemplate.KIE_SERVER_DATABASE_S2I_AMQ.getTemplateUrl(), envVariables);

        // Reuse same environment variables for second template
        logger.info("Processing template and creating resources from {}", OpenShiftTemplate.CONSOLE_SMARTROUTER.getTemplateUrl());
        project.processTemplateAndCreateResources(OpenShiftTemplate.CONSOLE_SMARTROUTER.getTemplateUrl(), envVariables);

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);
        smartRouterDeployment = createSmartRouterDeployment(project);
        kieServerDeployment = createKieServerDeployment(project);
        databaseDeployment = new DatabaseDeploymentImpl(project);
        amqDeployment = createAmqDeployment(project);

        logger.info("Waiting for AMQ deployment to become ready.");
        amqDeployment.waitForScale();

        logger.info("Waiting for Database deployment to become ready.");
        databaseDeployment.waitForScale();

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.scale(1);
        kieServerDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

        logger.info("Waiting for Workbench runtime deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logNodeNameOfAllInstances();
    }

    @Override public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<>(Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerDeployment, databaseDeployment, ssoDeployment, amqDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
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
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Arrays.asList(smartRouterDeployment);
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SsoDeployment getSsoDeployment() {
        return ssoDeployment;
    }

    @Override
    public AmqDeployment getAmqDeployment() {
        return amqDeployment;
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchRuntimeDeploymentImpl deployment = new WorkbenchRuntimeDeploymentImpl(project);
        deployment.setUsername(DeploymentConstants.getAppUser());
        deployment.setPassword(DeploymentConstants.getAppPassword());

        return deployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project) {
        SmartRouterDeploymentImpl deployment = new SmartRouterDeploymentImpl(project);
        deployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return deployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project) {
        KieServerDeploymentImpl deployment = new KieServerDeploymentImpl(project);
        deployment.setUsername(DeploymentConstants.getAppUser());
        deployment.setPassword(DeploymentConstants.getAppPassword());

        return deployment;
    }

    private AmqDeploymentImpl createAmqDeployment(Project project) {
        AmqDeploymentImpl deployment = new AmqDeploymentImpl(project);
        deployment.setUsername(DeploymentConstants.getAmqUsername());
        deployment.setPassword(DeploymentConstants.getAmqPassword());

        return deployment;
    }
}

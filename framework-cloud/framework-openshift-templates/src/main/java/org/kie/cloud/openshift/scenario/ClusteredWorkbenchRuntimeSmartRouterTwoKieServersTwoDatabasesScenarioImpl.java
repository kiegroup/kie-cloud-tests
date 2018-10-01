/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.template.ProjectProfile;
import org.kie.cloud.openshift.util.SsoDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl extends OpenShiftScenario implements ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario {

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerOneDeployment;
    private KieServerDeploymentImpl kieServerTwoDeployment;
    private DatabaseDeploymentImpl databaseOneDeployment;
    private DatabaseDeploymentImpl databaseTwoDeployment;
    private SsoDeployment ssoDeployment;

    private Map<String, String> envVariables;
    private boolean deploySso;
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();

    private static final Logger logger = LoggerFactory.getLogger(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl.class);

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(Map<String, String> envVariables, boolean deploySso) {
        this.envVariables = envVariables;
        this.deploySso=deploySso;
    }

    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(Map<String, String> envVariables) {
        this(envVariables, false);
    }

    @Override
    public void deploy() {
        super.deploy();

        if (deploySso) {
            ssoDeployment = SsoDeployer.deploy(project);

            envVariables.put(OpenShiftTemplateConstants.SSO_URL, SsoDeployer.createSsoEnvVariable(ssoDeployment.getUrl().toString()));
            envVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.getSsoRealm());

            ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
            envVariables.put(propertyNames.workbenchSsoClient(), projectProfile.getWorkbenchName() + "-client");
            envVariables.put(propertyNames.workbenchSsoSecret(), projectProfile.getWorkbenchName() + "-secret");

            //TODO create SSO client for both Kie Servers
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
            envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");
        }

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.CLUSTERED_CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        envVariables.put(OpenShiftTemplateConstants.POSTGRESQL_IMAGE_STREAM_NAMESPACE, projectName);
        project.processTemplateAndCreateResources(OpenShiftTemplate.CLUSTERED_CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES.getTemplateUrl(), envVariables);

        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);
        workbenchRuntimeDeployment.scale(1);

        smartRouterDeployment = createSmartRouterDeployment(project);
        smartRouterDeployment.scale(1);

        kieServerOneDeployment = createKieServerDeployment(project, "1");
        kieServerOneDeployment.scale(1);

        kieServerTwoDeployment = createKieServerDeployment(project, "2");
        kieServerTwoDeployment.scale(1);

        databaseOneDeployment = createDatabaseDeployment(project, "1");
        databaseTwoDeployment = createDatabaseDeployment(project, "2");

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchRuntimeDeployment.waitForScale();

        logger.info("Waiting for Smart router deployment to become ready.");
        smartRouterDeployment.waitForScale();

        logger.info("Waiting for Kie server one deployment to become ready.");
        kieServerOneDeployment.waitForScale();

        logger.info("Waiting for Kie server two deployment to become ready.");
        kieServerTwoDeployment.waitForScale();

        logger.info("Waiting for Database one deployment to become ready.");
        databaseOneDeployment.waitForScale();

        logger.info("Waiting for Database two deployment to become ready.");
        databaseTwoDeployment.waitForScale();

        logger.info("Waiting for Kie servers and Smart router to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchRuntimeDeployment, 3);

        logNodeNameOfAllInstances();
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
    public KieServerDeployment getKieServerOneDeployment() {
        return kieServerOneDeployment;
    }

    @Override
    public KieServerDeployment getKieServerTwoDeployment() {
        return kieServerTwoDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseOneDeployment() {
        return databaseOneDeployment;
    }

    @Override
    public DatabaseDeployment getDatabaseTwoDeployment() {
        return databaseTwoDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchRuntimeDeployment, smartRouterDeployment, kieServerOneDeployment, kieServerTwoDeployment, databaseOneDeployment, databaseTwoDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private WorkbenchDeployment createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchRuntimeDeploymentImpl workbenchRuntimeDeployment = new WorkbenchRuntimeDeploymentImpl(project);
        workbenchRuntimeDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchRuntimeDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        return workbenchRuntimeDeployment;
    }

    private SmartRouterDeployment createSmartRouterDeployment(Project project) {
        SmartRouterDeploymentImpl smartRouterDeployment = new SmartRouterDeploymentImpl(project);
        smartRouterDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

        return smartRouterDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project, String kieServerSuffix) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceSuffix("-" + kieServerSuffix);

        return kieServerDeployment;
    }

    private DatabaseDeploymentImpl createDatabaseDeployment(Project project, String databaseSuffix) {
        DatabaseDeploymentImpl databaseDeployment = new DatabaseDeploymentImpl(project);
        databaseDeployment.setServiceSuffix("-" + databaseSuffix);
        return databaseDeployment;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchRuntimeDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerOneDeployment, kieServerTwoDeployment);
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
}

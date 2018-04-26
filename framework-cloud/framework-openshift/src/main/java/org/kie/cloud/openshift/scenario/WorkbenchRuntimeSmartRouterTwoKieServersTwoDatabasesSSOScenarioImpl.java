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

import cz.xtf.sso.api.SsoApi;
import cz.xtf.sso.api.SsoApiFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SSODeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.DatabaseDeploymentImpl;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SSODeploymentImpl;
import org.kie.cloud.openshift.deployment.SmartRouterDeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchRuntimeDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenarioImpl extends OpenShiftScenario implements WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenario {

    private WorkbenchDeployment workbenchRuntimeDeployment;
    private SmartRouterDeployment smartRouterDeployment;
    private KieServerDeploymentImpl kieServerOneDeployment;
    private KieServerDeploymentImpl kieServerTwoDeployment;
    private DatabaseDeploymentImpl databaseOneDeployment;
    private DatabaseDeploymentImpl databaseTwoDeployment;
    private SSODeployment ssoDeployment;

    private Map<String, String> envVariables;
    private Map<String, String> ssoEnvVariables;

    private static final String SSO_REALM = DeploymentConstants.gettSSORealm();

    private static final String ADMIN = "admin", KIE_SERVER = "kie-server", REST_ALL = "rest-all";

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenarioImpl.class);

    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenarioImpl(Map<String, String> envVariables, Map<String, String> ssoEnvVariables) {
        this.envVariables = envVariables;
        this.ssoEnvVariables = ssoEnvVariables;
    }

    @Override
    public void deploy() {
        super.deploy();

        ssoDeployment = createSSODeployment(project);
        workbenchRuntimeDeployment = createWorkbenchRuntimeDeployment(project);
        smartRouterDeployment = createSmartRouterDeployment(project);
        kieServerOneDeployment = createKieServerDeployment(project, "1");
        kieServerTwoDeployment = createKieServerDeployment(project, "2");
        databaseOneDeployment = createDatabaseDeployment(project, "1");
        databaseTwoDeployment = createDatabaseDeployment(project, "2");

        logger.info("Creating SSO secrets from " + OpenShiftTemplate.SSO_SECRET.getTemplateUrl().toString());
        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO_SECRET.getTemplateUrl(), Collections.emptyMap());

        logger.info("Creating SSO image streams from " + OpenShiftConstants.getSSOImageStreams());
        project.createResources(OpenShiftConstants.getSSOImageStreams());

        logger.info("Processing template and createing resources from " + OpenShiftTemplate.SSO.getTemplateUrl().toString());
        Map<String, String> ssoEnvVariables = new HashMap<>(this.ssoEnvVariables);
        ssoEnvVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);

        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO.getTemplateUrl(), ssoEnvVariables);

        logger.info("Waiting for SSO deployment to become ready.");
        ssoDeployment.waitForScale();

        createRolesAndUsers(ssoDeployment.getUrl().toString() + "/auth", SSO_REALM);

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);

        envVariables.put(OpenShiftTemplateConstants.SSO_URL, createSSOEnvVariable(ssoDeployment.getUrl().toString()));
        envVariables.put(OpenShiftTemplateConstants.SSO_REALM, SSO_REALM);
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_CLIENT, "business-central-client");
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_SECRET, "business-central-secret");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER1_SSO_CLIENT, "kie-server1-client");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER1_SSO_SECRET, "kie-server1-secret");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER2_SSO_CLIENT, "kie-server2-client");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER2_SSO_SECRET, "kie-server2-secret");

        project.processTemplateAndCreateResources(OpenShiftTemplate.CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES.getTemplateUrl(), envVariables);

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

    // RHPAM-1228
    private String createSSOEnvVariable(String url) {
        String[] urlParts = url.split(":");
        return urlParts[0] + ":" + urlParts[1] + "/auth";
    }

    private void createRolesAndUsers(String authUrl, String realm) {
        logger.info("Creating roles and users in SSO at URL {} in Realm {}", authUrl, realm);
        SsoApi ssoApi = SsoApiFactory.getRestApi(authUrl, realm);

        ssoApi.createRole(ADMIN);
        ssoApi.createRole(KIE_SERVER);
        ssoApi.createRole(REST_ALL);
        ssoApi.createUser(envVariables.getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser()),
                envVariables.getOrDefault(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword()),
                Arrays.asList(ADMIN, KIE_SERVER, REST_ALL));
        ssoApi.createUser(envVariables.getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser()),
                envVariables.getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword()),
                Arrays.asList(KIE_SERVER, REST_ALL));
        ssoApi.createUser(envVariables.getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser()),
                envVariables.getOrDefault(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword()),
                Arrays.asList(KIE_SERVER));
        ssoApi.createUser(envVariables.getOrDefault(OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_USERNAME, MavenConstants.getMavenRepoUser()),
                envVariables.getOrDefault(OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_PASSWORD, MavenConstants.getMavenRepoPassword()));
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
        workbenchRuntimeDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());

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

    private SSODeployment createSSODeployment(Project project) {
        SSODeploymentImpl ssoDeploymnet = new SSODeploymentImpl(project);
        ssoDeploymnet.setUsername(DeploymentConstants.getSSOAdminUser());
        ssoDeploymnet.setPassword(DeploymentConstants.getSSOAdminPassword());

        return ssoDeploymnet;
    }

    @Override
    public SSODeployment getSSODeployment() {
        return ssoDeployment;
    }

}

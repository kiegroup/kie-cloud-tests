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
import java.util.List;
import java.util.Map;
import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SSODeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentSSOScenario;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.deployment.SSODeploymentImpl;
import org.kie.cloud.openshift.deployment.WorkbenchDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchKieServerPersistentSSOScenarioImpl extends OpenShiftScenario implements WorkbenchKieServerPersistentSSOScenario {

    private WorkbenchDeploymentImpl workbenchDeployment;
    private KieServerDeploymentImpl kieServerDeployment;
    private SSODeployment ssoDeployment;

    private Map<String, String> envVariables;
    private Map<String, String> ssoEnvVariables;

    private static final String SSO_REALM = DeploymentConstants.gettSSORealm();

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentSSOScenarioImpl.class);

    private static final String ADMIN = "admin", KIE_SERVER = "kie-server", REST_ALL = "rest-all";

    public WorkbenchKieServerPersistentSSOScenarioImpl(Map<String, String> envVariables, Map<String, String> ssoEnvVariables) {
        this.envVariables = envVariables;
        this.ssoEnvVariables = ssoEnvVariables;
    }

    @Override
    public void deploy() {
        super.deploy();
        ssoDeployment = createSSODeployment(project);
        workbenchDeployment = createWorkbenchRuntimeDeployment(project);
        kieServerDeployment = createKieServerDeployment(project);

        logger.info("Creating SSO secrets from " + OpenShiftTemplate.SSO_SECRET.getTemplateUrl().toString());
        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO_SECRET.getTemplateUrl(), Collections.emptyMap());
        logger.info("Creating SSO image streams from " + OpenShiftConstants.getSSOImageStreams());
        project.createResources(OpenShiftConstants.getSSOImageStreams());

        logger.info("Processing template and createing resources from " + OpenShiftTemplate.SSO.getTemplateUrl().toString());
        ssoEnvVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);

        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO.getTemplateUrl(), ssoEnvVariables);

        logger.info("Waiting for SSO deployment to become ready.");
        ssoDeployment.waitForScale();

        createRolesAndUsers(ssoDeployment.getUrl().toString() + "/auth", SSO_REALM);

        logger.info("Processing template and creating resources from " + OpenShiftTemplate.WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, projectName);
        envVariables.put(OpenShiftTemplateConstants.SSO_URL, createSSOEnvVariable(ssoDeployment.getUrl().toString()));
        envVariables.put(OpenShiftTemplateConstants.SSO_REALM, SSO_REALM);
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_CLIENT, "business-central-client");
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_SSO_SECRET, "business-central-secret");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_CLIENT, "kie-server-client");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SSO_SECRET, "kie-server-secret");

        project.processTemplateAndCreateResources(OpenShiftTemplate.WORKBENCH_KIE_SERVER_PERSISTENT.getTemplateUrl(), envVariables);

        logger.info("Waiting for Workbench deployment to become ready.");
        workbenchDeployment.waitForScale();
        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();
        logger.info("Waiting for Kie servers and Smart router to register itself to the Workbench.");
        KieServerControllerClientProvider.waitForServerTemplateCreation(workbenchDeployment, 1);

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
    public WorkbenchDeployment getWorkbenchDeployment() {
        return workbenchDeployment;
    }

    @Override
    public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override
    public List<Deployment> getDeployments() {
        List<Deployment> deployments = new ArrayList<Deployment>(Arrays.asList(workbenchDeployment, kieServerDeployment, ssoDeployment));
        deployments.removeAll(Collections.singleton(null));
        return deployments;
    }

    private WorkbenchDeploymentImpl createWorkbenchRuntimeDeployment(Project project) {
        WorkbenchDeploymentImpl workbenchDeployment = new WorkbenchDeploymentImpl(project);
        workbenchDeployment.setUsername(DeploymentConstants.getWorkbenchUser());
        workbenchDeployment.setPassword(DeploymentConstants.getWorkbenchPassword());

        return workbenchDeployment;
    }

    private KieServerDeploymentImpl createKieServerDeployment(Project project) {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project);
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());

        return kieServerDeployment;
    }

    private SSODeployment createSSODeployment(Project project) {
        SSODeploymentImpl ssoDeploymnet = new SSODeploymentImpl(project);
        ssoDeploymnet.setUsername(DeploymentConstants.getSSOAdminUser());
        ssoDeploymnet.setPassword(DeploymentConstants.getSSOAdminPassword());

        return ssoDeploymnet;
    }

    @Override
    public List<WorkbenchDeployment> getWorkbenchDeployments() {
        return Arrays.asList(workbenchDeployment);
    }

    @Override
    public List<KieServerDeployment> getKieServerDeployments() {
        return Arrays.asList(kieServerDeployment);
    }

    @Override
    public List<SmartRouterDeployment> getSmartRouterDeployments() {
        return Collections.emptyList();
    }

    @Override
    public List<ControllerDeployment> getControllerDeployments() {
        return Collections.emptyList();
    }

    @Override
    public SSODeployment getSSODeployment() {
        return ssoDeployment;
    }

}

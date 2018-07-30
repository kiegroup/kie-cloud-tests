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
package org.kie.cloud.openshift.util;

import cz.xtf.sso.api.SsoApi;
import cz.xtf.sso.api.SsoApiFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.kie.cloud.api.deployment.SSODeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.SSODeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSODeployer {

    private static final Logger logger = LoggerFactory.getLogger(SSODeployer.class);
    private static final String SSO_REALM = DeploymentConstants.gettSSORealm();
    private static final String ADMIN = "admin", KIE_SERVER = "kie-server", REST_ALL = "rest-all";

    public static SSODeployment deploy(Project project, Map<String, String> envVariables, Map<String, String> ssoEnvVariables) {
        SSODeployment ssoDeployment = createSSODeployment(project);

        logger.info("Creating SSO secrets from " + OpenShiftTemplate.SSO_SECRET.getTemplateUrl().toString());
        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO_SECRET.getTemplateUrl(), Collections.emptyMap());
        logger.info("Creating SSO image streams from " + OpenShiftConstants.getSSOImageStreams());
        project.createResources(OpenShiftConstants.getSSOImageStreams());

        logger.info("Processing template and createing resources from " + OpenShiftTemplate.SSO.getTemplateUrl().toString());
        ssoEnvVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, project.getName());

        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO.getTemplateUrl(), ssoEnvVariables);

        logger.info("Waiting for SSO deployment to become ready.");
        ssoDeployment.waitForScale();

        createRolesAndUsers(ssoDeployment.getUrl().toString() + "/auth", SSO_REALM, envVariables);

        return ssoDeployment;
    }

    private static SSODeployment createSSODeployment(Project project) {
        SSODeploymentImpl ssoDeploymnet = new SSODeploymentImpl(project);
        ssoDeploymnet.setUsername(DeploymentConstants.getSSOAdminUser());
        ssoDeploymnet.setPassword(DeploymentConstants.getSSOAdminPassword());

        return ssoDeploymnet;
    }

    // RHPAM-1228
    public static String createSSOEnvVariable(String url) {
        String[] urlParts = url.split(":");
        return urlParts[0] + ":" + urlParts[1] + "/auth";
    }

    private static void createRolesAndUsers(String authUrl, String realm, Map<String, String> envVariables) {
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
}

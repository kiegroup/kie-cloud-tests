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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.kie.cloud.api.deployment.SsoDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.SsoTemplateConstants;
import org.kie.cloud.openshift.deployment.SsoDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.kie.cloud.openshift.util.sso.SsoApi;
import org.kie.cloud.openshift.util.sso.SsoApiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoDeployer {

    private static final Logger logger = LoggerFactory.getLogger(SsoDeployer.class);
    private static final String SSO_REALM = DeploymentConstants.getSsoRealm();
    private static final String ADMIN = "admin", KIE_SERVER = "kie-server", REST_ALL = "rest-all";

    /**
     * Deploy and configure SSO using HTTP route.
     *
     * @param project
     * @return
     */
    public static SsoDeployment deploy(Project project) {
        SsoDeployment ssoDeployment = deploySsoTemplate(project);

        createRolesAndUsers(ssoDeployment.getUrl().toString() + "/auth", SSO_REALM);

        return ssoDeployment;
    }

    /**
     * Deploy and configure SSO using HTTPS route.
     *
     * @param project
     * @return
     */
    public static SsoDeployment deploySecure(Project project) {
        SsoDeployment ssoDeployment = deploySsoTemplate(project);

        URL ssoSecureUrl = ssoDeployment.getSecureUrl().orElseThrow(() -> new RuntimeException("RH SSO secure URL not found."));
        createRolesAndUsers(ssoSecureUrl.toString() + "/auth", SSO_REALM);

        return ssoDeployment;
    }

    private static SsoDeployment deploySsoTemplate(Project project) {
        SsoDeployment ssoDeployment = createSsoDeployment(project);

        logger.info("Creating SSO image streams in namespace \"openshift\" from " + OpenShiftConstants.getSsoImageStreams());
        imageStreamDeploy(project);
        logger.info("Creating SSO secrets from " + OpenShiftTemplate.SSO_SECRET.getTemplateUrl().toString());
        project.createResources(OpenShiftTemplate.SSO_SECRET.getTemplateUrl().toExternalForm());

        logger.info("Processing template and createing resources from " + OpenShiftTemplate.SSO.getTemplateUrl().toString());
        Map<String, String> ssoEnvVariables = new HashMap<>();
        ssoEnvVariables.put(SsoTemplateConstants.SSO_ADMIN_USERNAME, DeploymentConstants.getSsoAdminUser());
        ssoEnvVariables.put(SsoTemplateConstants.SSO_ADMIN_PASSWORD, DeploymentConstants.getSsoAdminPassword());
        ssoEnvVariables.put(SsoTemplateConstants.SSO_REALM, DeploymentConstants.getSsoRealm());
        ssoEnvVariables.put(SsoTemplateConstants.SSO_SERVICE_USERNAME, DeploymentConstants.getSsoServiceUser());
        ssoEnvVariables.put(SsoTemplateConstants.SSO_SERVICE_PASSWORD, DeploymentConstants.getSsoServicePassword());
        ssoEnvVariables.put(SsoTemplateConstants.HTTPS_NAME, "jboss");
        ssoEnvVariables.put(SsoTemplateConstants.HTTPS_PASSWORD, "mykeystorepass");
        project.processTemplateAndCreateResources(OpenShiftTemplate.SSO.getTemplateUrl(), ssoEnvVariables);

        logger.info("Waiting for SSO deployment to become ready.");
        ssoDeployment.waitForScale();

        return ssoDeployment;
    }

    private static void imageStreamDeploy(Project project) {
        try {
            OpenShift openShift = OpenShifts.admin(project.getName());
            KubernetesList resourceList = openShift.lists().inNamespace("openshift").load(new URL(OpenShiftConstants.getSsoImageStreams())).get();
            resourceList.getItems().forEach(item -> {
                openShift.imageStreams().inNamespace("openshift").withName(item.getMetadata().getName()).delete();
            });
            openShift.lists().inNamespace("openshift").create(resourceList);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        } catch (KubernetesClientException e) {
            if (e.getMessage().contains("AlreadyExists")) {
                // Image stream was already recreated, ignoring exception.
            } else {
                throw new RuntimeException("Error while deploying SSO image stream.", e);
            }
        }
    }

    private static SsoDeployment createSsoDeployment(Project project) {
        SsoDeploymentImpl ssoDeploymnet = new SsoDeploymentImpl(project);
        ssoDeploymnet.setUsername(DeploymentConstants.getSsoAdminUser());
        ssoDeploymnet.setPassword(DeploymentConstants.getSsoAdminPassword());

        return ssoDeploymnet;
    }

    // RHPAM-1228
    public static String createSsoEnvVariable(String url) {
        String[] urlParts = url.split(":");
        return urlParts[0] + ":" + urlParts[1] + "/auth";
    }

    private static void createRolesAndUsers(String authUrl, String realm) {
        logger.info("Creating roles and users in SSO at URL {} in Realm {}", authUrl, realm);
        SsoApi ssoApi = SsoApiFactory.getRestApi(authUrl, realm);

        ssoApi.createRole(ADMIN);
        ssoApi.createRole(KIE_SERVER);
        ssoApi.createRole(REST_ALL);
        ssoApi.createUser(DeploymentConstants.getWorkbenchUser(),
                DeploymentConstants.getWorkbenchPassword(),
                Arrays.asList(ADMIN, KIE_SERVER, REST_ALL));
        ssoApi.createUser(DeploymentConstants.getControllerUser(),
                DeploymentConstants.getControllerPassword(),
                Arrays.asList(KIE_SERVER, REST_ALL));
        ssoApi.createUser(DeploymentConstants.getKieServerUser(),
                DeploymentConstants.getKieServerPassword(),
                Arrays.asList(KIE_SERVER));
        ssoApi.createUser(DeploymentConstants.getWorkbenchMavenUser(),
                DeploymentConstants.getWorkbenchMavenPassword());
    }

    public static void createUsers(SsoDeployment ssoDeployment, Map<String, String> users) {
        String authUrl = ssoDeployment.getUrl().toString() + "/auth"; 
        logger.info("Creating custom users in SSO at URL {} in Realm {}", authUrl, SSO_REALM);
        SsoApi ssoApi = SsoApiFactory.getRestApi(authUrl, SSO_REALM);

        users.forEach((String name, String pwd) -> {
            ssoApi.createUser(name, pwd, Arrays.asList(ADMIN, KIE_SERVER, REST_ALL));
        });
    }
}

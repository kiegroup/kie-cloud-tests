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
package org.kie.cloud.openshift.settings.builder;

import java.util.HashMap;
import java.util.Map;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerSettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class KieServerMySqlSettingsBuilderImpl implements KieServerSettingsBuilder {

    private Map<String, String> envVariables;
    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.KIE_SERVER_MYSQL;

    public KieServerMySqlSettingsBuilderImpl() {
        envVariables = new HashMap<>();

        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsImpl(envVariables, appTemplate);
    }

    @Override
    public KieServerSettingsBuilder withApplicationName(String name) {
        envVariables.put(OpenShiftTemplateConstants.APPLICATION_NAME, name);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withKieServerUser(String kieServerUser, String kieServerPwd) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, kieServerUser);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, kieServerPwd);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withAdminUser(String user, String password) {
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, user);
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, password);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withControllerUser(String controllerUser, String controllerPwd) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, controllerUser);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, controllerPwd);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withControllerConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withControllerConnection(String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, port);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withControllerConnection(String protocol, String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PROTOCOL, protocol);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, port);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withSmartRouterConnection(String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, port);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withSmartRouterConnection(String protocol, String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PROTOCOL, protocol);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, port);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withSmartRouterConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withMavenRepoUrl(String url) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, url);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withMavenRepoService(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withMavenRepoServiceUser(String workbenchMavenUser, String workbenchMavenPassword) {
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_USERNAME, workbenchMavenUser);
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_PASSWORD, workbenchMavenPassword);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withMavenRepoService(String service, String path) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_SERVICE, service);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PATH, path);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withMavenRepoUser(String repoUser, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUser);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withKieServerSyncDeploy(boolean syncDeploy) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SYNC_DEPLOY, Boolean.toString(syncDeploy));
        return this;
    }

    @Override
    public KieServerSettingsBuilder withKieServerBypassAuthUser(boolean bypassAuth) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_BYPASS_AUTH_USER, Boolean.toString(bypassAuth));
        return this;
    }

    @Override
    public KieServerSettingsBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }

    @Override
    public KieServerSettingsBuilder withHostame(String http) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withSecuredHostame(String https) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public KieServerSettingsBuilder withKieServerSecret(String secret) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, secret);
        return this;
    }

}

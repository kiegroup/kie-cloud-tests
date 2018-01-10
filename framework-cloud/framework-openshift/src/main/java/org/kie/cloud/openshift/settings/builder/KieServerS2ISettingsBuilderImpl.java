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
package org.kie.cloud.openshift.settings.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class KieServerS2ISettingsBuilderImpl implements KieServerS2ISettingsBuilder {

    private Map<String, String> envVariables;
    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.KIE_SERVER_S2I;

    public KieServerS2ISettingsBuilderImpl() {
        envVariables = new HashMap<>();

        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsImpl(envVariables, appTemplate);
    }

    @Override
    public KieServerS2ISettingsBuilder withApplicationName(String name) {
        envVariables.put(OpenShiftTemplateConstants.APPLICATION_NAME, name);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withKieServerUser(String kieServerUser, String kieServerPwd) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, kieServerUser);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, kieServerPwd);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerUser(String controllerUser, String controllerPwd) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, controllerUser);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, controllerPwd);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerProtocol(Protocol protocol) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PROTOCOL, protocol.name());
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withControllerConnection(String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, port);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSmartRouterConnection(String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, port);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSmartRouterConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withExternalDatabase() {
        envVariables.put(OpenShiftTemplateConstants.DBE_SERVICE_HOST, DeploymentConstants.getDatabaseHost());
        envVariables.put(OpenShiftTemplateConstants.DBE_DATABASE, DeploymentConstants.getExternalDatabaseName());
        envVariables.put(OpenShiftTemplateConstants.DBE_USERNAME, DeploymentConstants.getDatabaseUsername());
        envVariables.put(OpenShiftTemplateConstants.DBE_PASSWORD, DeploymentConstants.getDatabasePassword());
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        envVariables.put(OpenShiftTemplateConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withMavenRepoUrl(String url) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, url);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withMavenRepoService(String service, String path) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_SERVICE, service);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PATH, path);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withMavenRepoUser(String repoUser, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUser);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withKieServerSyncDeploy(boolean syncDeploy) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SYNC_DEPLOY, Boolean.toString(syncDeploy));
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withHostame(String http) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withSecuredHostame(String https) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public KieServerS2ISettingsBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }
}

/*
 * Copyright 2019 JBoss by Red Hat.
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
import org.kie.cloud.api.settings.builder.KieServerS2IAmqSettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class KieServerS2IAmqSettingsBuilderImpl implements KieServerS2IAmqSettingsBuilder {

    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.KIE_SERVER_S2I_AMQ;
    private Map<String, String> envVariables;

    public KieServerS2IAmqSettingsBuilderImpl() {
        envVariables = new HashMap<>();

        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        
        envVariables.put(OpenShiftTemplateConstants.AMQ_USERNAME, DeploymentConstants.getAmqUsername());
        envVariables.put(OpenShiftTemplateConstants.AMQ_PASSWORD, DeploymentConstants.getAmqPassword());
        envVariables.put(OpenShiftTemplateConstants.AMQ_SECRET, "amq-app-secret");
        envVariables.put(OpenShiftTemplateConstants.AMQ_TRUSTSTORE, "broker.ts");
        envVariables.put(OpenShiftTemplateConstants.AMQ_TRUSTSTORE_PASSWORD, "changeit");
        envVariables.put(OpenShiftTemplateConstants.AMQ_KEYSTORE, "broker.ks");
        envVariables.put(OpenShiftTemplateConstants.AMQ_KEYSTORE_PASSWORD, "changeit");
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsImpl(envVariables, appTemplate);
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withApplicationName(String name) {
        envVariables.put(OpenShiftTemplateConstants.APPLICATION_NAME, name);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withKieServerUser(String kieServerUser, String kieServerPwd) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, kieServerUser);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, kieServerPwd);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withControllerUser(String controllerUser, String controllerPwd) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, controllerUser);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, controllerPwd);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withControllerProtocol(Protocol protocol) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PROTOCOL, protocol.name());
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withControllerConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withControllerConnection(String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PORT, port);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withSmartRouterConnection(String url, String port) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, url);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, port);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withSmartRouterConnection(String serviceName) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_SERVICE, serviceName);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        envVariables.put(OpenShiftTemplateConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withMavenRepoUrl(String url) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, url);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withMavenRepoUser(String repoUser, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUser);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withKieServerSyncDeploy(boolean syncDeploy) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_SYNC_DEPLOY, Boolean.toString(syncDeploy));
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withHostame(String http) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withSecuredHostame(String https) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }

    @Override
    public KieServerS2IAmqSettingsBuilder withKieServerSecret(String secret) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, secret);
        return this;
    }
}

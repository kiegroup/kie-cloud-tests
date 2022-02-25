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
package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerPersistentScenarioImpl;

public class WorkbenchKieServerPersistentScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderTemplates<WorkbenchKieServerPersistentScenario> implements WorkbenchKieServerPersistentScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
    private final ScenarioRequest request = new ScenarioRequest();

    public WorkbenchKieServerPersistentScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(propertyNames.workbenchHttpsSecret(), OpenShiftConstants.getKieApplicationSecretName());
    }

    @Override
    public WorkbenchKieServerPersistentScenario getDeploymentScenarioInstance() {
        return new WorkbenchKieServerPersistentScenarioImpl(envVariables, request);
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder usePublicIpAddress() {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE, Boolean.FALSE.toString());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder deploySso() {
        request.enableDeploySso();
        envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
        envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        envVariables.put(propertyNames.workbenchHostnameHttp(), hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        envVariables.put(propertyNames.workbenchHostnameHttps(), hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir) {
        envVariables.put(OpenShiftTemplateConstants.GIT_HOOKS_DIR, dir);
        return this;
    }


    @Override 
    public WorkbenchKieServerPersistentScenarioBuilder withReposPersistence() {
        throw new UnsupportedOperationException("Not supported with templates.");
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder withRoleMapper(String rolesProperties, Boolean rolesKeepMapped, Boolean rolesKeepNonMapped) {
        throw new UnsupportedOperationException("Not supported with templates.");
    }
}

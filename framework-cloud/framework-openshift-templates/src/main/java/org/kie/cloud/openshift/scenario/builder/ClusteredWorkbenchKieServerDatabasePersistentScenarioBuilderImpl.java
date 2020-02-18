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

package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenarioImpl;

public class ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderTemplates<ClusteredWorkbenchKieServerDatabasePersistentScenario> implements
                                                                              ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private boolean deploySso = false;
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();

    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());

        ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
        envVariables.put(propertyNames.workbenchHttpsSecret(), OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(propertyNames.workbenchMemoryLimit(), "2Gi"); // limit memory limit to use only 4Gi
    }

    @Override
    protected ClusteredWorkbenchKieServerDatabasePersistentScenario getDeploymentScenarioInstance() {
        return new ClusteredWorkbenchKieServerDatabasePersistentScenarioImpl(envVariables, deploySso);
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withGitHooksDir(String dir) {
        envVariables.put(OpenShiftTemplateConstants.GIT_HOOKS_DIR, dir);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder deploySso() {
        deploySso = true;
        envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
        envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpWorkbenchHostname(String hostname) {
        envVariables.put(propertyNames.workbenchHostnameHttp(), hostname);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname) {
        envVariables.put(propertyNames.workbenchHostnameHttps(), hostname);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, hostname);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpsKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withExternalLdap(LdapSettings ldapSettings) {
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withInternalLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withWorkbenchMemoryLimit(String limit) {
        envVariables.put(propertyNames.workbenchMemoryLimit(), limit);
        return this;
    }
}

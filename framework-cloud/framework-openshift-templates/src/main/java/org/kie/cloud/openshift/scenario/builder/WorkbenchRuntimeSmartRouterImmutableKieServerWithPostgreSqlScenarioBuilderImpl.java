/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioImpl;

public class WorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilderImpl extends KieScenarioBuilderImpl<WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder, WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario> implements WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
    private ScenarioRequest request = new ScenarioRequest();

    public WorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(propertyNames.workbenchHttpsSecret(), OpenShiftConstants.getKieApplicationSecretName());

        // TODO: Workaround until Maven repo with released artifacts is implemented
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_MODE, "DEVELOPMENT");
    }

    @Override
    protected WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario getDeploymentScenarioInstance() {
        return new WorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioImpl(envVariables, request);
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withSourceLocation(String gitReference, String gitContextDir) {
        if (request.getGitSettings() == null) {
            throw new RuntimeException("Need to configure the git settings first");
        }

        request.getGitSettings().addOnRepositoryLoaded(gitRepoUrl -> envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl));
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withSourceLocation(String gitReference, String gitContextDir, String artifactDirs) {
        withSourceLocation(gitReference, gitContextDir);
        envVariables.put(OpenShiftTemplateConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder deploySso() {
        request.enableDeploySso();
        envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
        envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withHttpKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, hostname);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withHttpsKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withTimerServiceDataStoreRefreshInterval(
            Duration timerServiceDataStoreRefreshInterval) {
        envVariables.put(OpenShiftTemplateConstants.TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL, Long.toString(timerServiceDataStoreRefreshInterval.toMillis()));
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withKieServerMemoryLimit(String limit) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_MEMORY_LIMIT, limit);
        return this;
    }
}

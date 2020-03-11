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

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ImmutableKieServerScenario;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerScenarioBuilder;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ImmutableKieServerScenarioImpl;
import org.kie.cloud.openshift.scenario.ScenarioRequest;

public class ImmutableKieServerScenarioBuilderImpl extends KieScenarioBuilderImpl<ImmutableKieServerScenarioBuilder, ImmutableKieServerScenario> implements ImmutableKieServerScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private ScenarioRequest request = new ScenarioRequest();

    public ImmutableKieServerScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());

        // TODO: Workaround until Maven repo with released artifacts is implemented
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_MODE, "DEVELOPMENT");
    }

    @Override
    protected ImmutableKieServerScenario getDeploymentScenarioInstance() {
        return new ImmutableKieServerScenarioImpl(envVariables, request);
    }

    @Override
    public ImmutableKieServerScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withSourceLocation(String gitReference, String gitContextDir) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);

        if (request.getGitSettings() == null) {
            throw new RuntimeException("Need to configure the git settings first");
        }

        request.getGitSettings().addOnRepositoryLoaded(gitRepoUrl -> envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl));
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withSourceLocation(String gitReference, String gitContextDir, String artifactDirs) {
        withSourceLocation(gitReference, gitContextDir);
        envVariables.put(OpenShiftTemplateConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder deploySso() {
        request.enableDeploySso();
        envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
        envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withHttpKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, hostname);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withHttpsKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withExternalLdap(LdapSettings ldapSettings) {
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }

    @Override
    public ImmutableKieServerScenarioBuilder withInternalLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }
}

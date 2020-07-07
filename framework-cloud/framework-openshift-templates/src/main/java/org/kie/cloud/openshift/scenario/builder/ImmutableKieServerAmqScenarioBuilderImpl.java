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
import org.kie.cloud.api.scenario.ImmutableKieServerAmqScenario;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerAmqScenarioBuilder;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ImmutableKieServerAmqScenarioImpl;
import org.kie.cloud.openshift.scenario.ScenarioRequest;

import static org.kie.cloud.openshift.util.ScenarioValidations.verifyDroolsScenarioOnly;

public class ImmutableKieServerAmqScenarioBuilderImpl extends KieScenarioBuilderImpl<ImmutableKieServerAmqScenarioBuilder, ImmutableKieServerAmqScenario> implements ImmutableKieServerAmqScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private ScenarioRequest request = new ScenarioRequest();

    public ImmutableKieServerAmqScenarioBuilderImpl() {
        verifyDroolsScenarioOnly();

        envVariables.put(OpenShiftTemplateConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());

        // TODO: Workaround until Maven repo with released artifacts is implemented
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_MODE, "DEVELOPMENT");

        envVariables.put(OpenShiftTemplateConstants.AMQ_USERNAME, DeploymentConstants.getAmqUsername());
        envVariables.put(OpenShiftTemplateConstants.AMQ_PASSWORD, DeploymentConstants.getAmqPassword());
        // These values are defined in pom.xml where keystore and truststore are generated
        envVariables.put(OpenShiftTemplateConstants.AMQ_SECRET, "amq-app-secret");
        envVariables.put(OpenShiftTemplateConstants.AMQ_TRUSTSTORE, "broker.ts");
        envVariables.put(OpenShiftTemplateConstants.AMQ_TRUSTSTORE_PASSWORD, "changeit");
        envVariables.put(OpenShiftTemplateConstants.AMQ_KEYSTORE, "broker.ks");
        envVariables.put(OpenShiftTemplateConstants.AMQ_KEYSTORE_PASSWORD, "changeit");
    }

    @Override
    protected ImmutableKieServerAmqScenario getDeploymentScenarioInstance() {
        return new ImmutableKieServerAmqScenarioImpl(envVariables, request);
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withSourceLocation(String gitReference, String gitContextDir) {
        if (request.getGitSettings() == null) {
            throw new RuntimeException("Need to configure the git settings first");
        }

        request.getGitSettings().addOnRepositoryLoaded(gitRepoUrl -> envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl));
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withSourceLocation(String gitReference, String gitContextDir, String artifactDirs) {
        withSourceLocation(gitReference, gitContextDir);
        envVariables.put(OpenShiftTemplateConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder deploySso() {
        request.enableDeploySso();
        envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
        envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withHttpKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, hostname);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withHttpsKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder withLdap(LdapSettings ldapSettings) {
        setAsyncExternalDeployment(ExternalDeploymentID.LDAP);
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }
}

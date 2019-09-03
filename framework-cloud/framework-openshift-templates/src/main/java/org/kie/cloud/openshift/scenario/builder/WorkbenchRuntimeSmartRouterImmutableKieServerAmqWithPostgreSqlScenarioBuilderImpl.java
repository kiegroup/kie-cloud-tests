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
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.constants.ProjectSpecificPropertyNames;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioImpl;

public class WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioBuilderImpl extends KieScenarioBuilderImpl<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder, WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> implements WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private final ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
    private boolean deploySso = false;

    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(propertyNames.workbenchHttpsSecret(), OpenShiftConstants.getKieApplicationSecretName());

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
    protected DeploymentScenario<WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario> getDeploymentScenarioInstance() {
        return new WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioImpl(envVariables, deploySso);
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withContainerDeployment(String kieContainerDeployment) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTAINER_DEPLOYMENT, kieContainerDeployment);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs) {
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_URL, gitRepoUrl);
        envVariables.put(OpenShiftTemplateConstants.SOURCE_REPOSITORY_REF, gitReference);
        envVariables.put(OpenShiftTemplateConstants.CONTEXT_DIR, gitContextDir);
        envVariables.put(OpenShiftTemplateConstants.ARTIFACT_DIR, artifactDirs);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder deploySso() {
        deploySso = true;
        envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSsoServiceUser());
        envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSsoServicePassword());
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withHttpKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTP, hostname);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withHttpsKieServerHostname(String hostname) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HOSTNAME_HTTPS, hostname);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter) {
        envVariables.put(OpenShiftTemplateConstants.DROOLS_SERVER_FILTER_CLASSES, Boolean.toString(droolsFilter));
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        envVariables.putAll(ldapSettings.getEnvVariables());
        return this;
    }
}

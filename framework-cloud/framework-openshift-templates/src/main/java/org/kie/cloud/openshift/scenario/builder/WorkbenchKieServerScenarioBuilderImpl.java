/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.scenario.ScenarioRequest;
import org.kie.cloud.openshift.scenario.WorkbenchKieServerScenarioImpl;

public class WorkbenchKieServerScenarioBuilderImpl extends AbstractOpenshiftScenarioBuilderTemplates<WorkbenchKieServerScenario> implements WorkbenchKieServerScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private ScenarioRequest request = new ScenarioRequest();

    public WorkbenchKieServerScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.CREDENTIALS_SECRET, DeploymentConstants.getAppCredentialsSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getAppUser());
        envVariables.put(OpenShiftTemplateConstants.DEFAULT_PASSWORD, DeploymentConstants.getAppPassword());
    }

    @Override
    public WorkbenchKieServerScenario getDeploymentScenarioInstance() {
        return new WorkbenchKieServerScenarioImpl(envVariables, request);
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withInternalMavenRepo() {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowCredentials(boolean allowCredentials) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(allowCredentials));
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowHeaders(String... allowedHeaders) {
        String allowedHeadersValue = Stream.of(allowedHeaders).collect(Collectors.joining(", "));
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ACCESS_CONTROL_ALLOW_HEADERS, allowedHeadersValue);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowMethods(String... allowedMethods) {
        String allowedMethodsValue = Stream.of(allowedMethods).collect(Collectors.joining(", "));
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ACCESS_CONTROL_ALLOW_METHODS, allowedMethodsValue);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlAllowOrigin(String url) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ACCESS_CONTROL_ALLOW_ORIGIN, url);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withAccessControlMaxAge(Duration maxAge) {
        String maxAgeInSeconds = Long.toString(maxAge.getSeconds());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ACCESS_CONTROL_MAX_AGE, maxAgeInSeconds);
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withPrometheusMonitoring() {
        request.enableDeployPrometheus();
        envVariables.put(OpenShiftTemplateConstants.PROMETHEUS_SERVER_EXT_DISABLED, "false");
        return this;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder withGitSettings(GitSettings gitSettings) {
        request.setGitSettings(gitSettings);
        return this;
    }
}

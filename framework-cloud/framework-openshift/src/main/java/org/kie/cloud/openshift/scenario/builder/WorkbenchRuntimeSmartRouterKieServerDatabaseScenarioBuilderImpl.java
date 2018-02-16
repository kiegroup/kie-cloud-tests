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

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl;

public class WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl implements WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();

    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl() {
        // TODO: Hardcoded because Workbench runtime Smart router template is designed to handle unmanaged Kie servers.
        // Therefore the template doesn't have possibility to set Kie server username/password for controller requests.
        // These values are default, used when controller username/password isn't defined.
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, "executionUser");
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, "execution1!");
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());

        // By default use Workbench as maven repo, repo URL is derived from Workbench automatically if not defined
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, DeploymentConstants.getWorkbenchPassword());
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenario build() {
        return new WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioImpl(envVariables);
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder withSmartRouterId(String smartRouterId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_ID, smartRouterId);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }
}

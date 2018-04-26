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
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenarioImpl;

public class WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilderImpl implements WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder {

    private final Map<String, String> kieEnvVariables = new HashMap<>();
    private final Map<String, String> ssoEnvVariables = new HashMap<>();

    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilderImpl() {
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
        kieEnvVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, DeploymentConstants.getWorkbenchUser());
        kieEnvVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, DeploymentConstants.getWorkbenchPassword());

        kieEnvVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSSOServiceUser());
        kieEnvVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSSOServicePassword());

        kieEnvVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());

        // SSO env variables
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_ADMIN_USERNAME, DeploymentConstants.getSSOAdminUser());
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_ADMIN_PASSWORD, DeploymentConstants.getSSOAdminPassword());

        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.gettSSORealm());

        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_SERVICE_USERNAME, DeploymentConstants.getSSOServiceUser());
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_SERVICE_PASSWORD, DeploymentConstants.getSSOServicePassword());
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        kieEnvVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        kieEnvVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        kieEnvVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withSmartRouterId(String smartRouterId) {
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_ID, smartRouterId);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withKieServerId(String kieServerId) {
        kieEnvVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ID, kieServerId);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpWorkbenchHostname(String http) {
        kieEnvVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpsWorkbenchHostname(String https) {
        kieEnvVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpKieServer1Hostname(String http) {
        kieEnvVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER1_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpsKieServer1Hostname(String https) {
        kieEnvVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER1_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpKieServer2Hostname(String http) {
        kieEnvVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER2_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpsKieServer2Hostname(String https) {
        kieEnvVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER2_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenario build() {
        return new WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenarioImpl(kieEnvVariables, ssoEnvVariables);
    }

}

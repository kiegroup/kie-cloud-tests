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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl;

public class WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl implements WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder {

    private final Map<String, String> envVariables = new HashMap<>();
    private boolean deploySSO = false;

    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl() {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_USER, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_ADMIN_PWD, DeploymentConstants.getWorkbenchPassword());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_USER, DeploymentConstants.getControllerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_CONTROLLER_PWD, DeploymentConstants.getControllerPassword());
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, DeploymentConstants.getWorkbenchUser());
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, DeploymentConstants.getWorkbenchPassword());
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());
    }

    private Map<String, String> getSSOenvVariables() {
        if (!deploySSO) {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> ssoEnvVariables = new HashMap<>();

        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_ADMIN_USERNAME, DeploymentConstants.getSSOAdminUser());
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_ADMIN_PASSWORD, DeploymentConstants.getSSOAdminPassword());
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_REALM, DeploymentConstants.gettSSORealm());
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_SERVICE_USERNAME, DeploymentConstants.getSSOServiceUser());
        ssoEnvVariables.put(OpenShiftTemplateConstants.SSO_SERVICE_PASSWORD, DeploymentConstants.getSSOServicePassword());

        return ssoEnvVariables;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario build() {
        return new WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioImpl(envVariables, deploySSO, getSSOenvVariables());
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId) {
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_ID, smartRouterId);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval) {
        envVariables.put(OpenShiftTemplateConstants.TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL, Long.toString(timerServiceDataStoreRefreshInterval.toMillis()));
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySSO(boolean deploySSO) {
        this.deploySSO = deploySSO;
        if (deploySSO) {
            envVariables.put(OpenShiftTemplateConstants.SSO_USERNAME, DeploymentConstants.getSSOServiceUser());
            envVariables.put(OpenShiftTemplateConstants.SSO_PASSWORD, DeploymentConstants.getSSOServicePassword());
        }
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withBusinessCentralMavenUser(String user, String password) {
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_USERNAME, user);
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_MAVEN_PASSWORD, password);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String http) {
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String https) {
        envVariables.put(OpenShiftTemplateConstants.BUSINESS_CENTRAL_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String http) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER1_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String https) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER1_HOSTNAME_HTTPS, https);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String http) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER2_HOSTNAME_HTTP, http);
        return this;
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String https) {
        envVariables.put(OpenShiftTemplateConstants.EXECUTION_SERVER2_HOSTNAME_HTTPS, https);
        return this;
    }

}

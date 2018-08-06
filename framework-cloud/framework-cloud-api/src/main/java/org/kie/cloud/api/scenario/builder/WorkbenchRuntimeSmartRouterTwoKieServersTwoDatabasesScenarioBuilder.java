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
package org.kie.cloud.api.scenario.builder;

import java.time.Duration;

import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;

/**
 * Cloud builder for Workbench runtime, Smart router and two Kie Servers with
 * two databases in project. Built setup for
 * WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario
 *
 * @see WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario
 */
public interface WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario> {

    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     *
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo user name.
     * @param repoPassword Maven repo user password.
     * @return Builder with configured external maven repo.
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * Return setup builder with specified Smart router id.
     *
     * @param smartRouterId Smart router id.
     * @return Builder with configured Smart router id.
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId);

    /**
     * Return setup builder with specified timer service data store refresh
     * interval.
     *
     * @param timerServiceDataStoreRefreshInterval timer service data store
     * refresh interval.
     * @return Builder with configured timer service data store refresh
     * interval.
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySso();

    /**
     * Return setup builder with Business Central user for the maven repository.
     *
     * @param user Business Central Maven repo user name.
     * @param password Business Central Maven repo user password.
     * @return Builder with configured Business Central Maven repo user.
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withBusinessCentralMavenUser(String user, String password);

    /**
     * Return setup builder with configure Workbench http hostname.
     *
     * @param hostname HTTP hostname for Workbench
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Workbench https hostname.
     *
     * @param hostname HTTPS hostname for Workbench
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 1 http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 1 https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 2 http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 2 https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String hostname);
}

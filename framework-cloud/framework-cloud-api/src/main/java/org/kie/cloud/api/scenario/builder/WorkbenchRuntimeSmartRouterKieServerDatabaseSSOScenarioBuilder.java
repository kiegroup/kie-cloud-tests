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
package org.kie.cloud.api.scenario.builder;

import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenario;

public interface WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesSSOScenario> {

    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     *
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo user name.
     * @param repoPassword Maven repo user password.
     * @return Builder with configured external maven repo.
     */
    WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * Return setup builder with specified Smart router id.
     *
     * @param smartRouterId Smart router id.
     * @return Builder with configured Smart router id.
     */
    WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withSmartRouterId(String smartRouterId);

    /**
     * @param kieServerId kie-server id
     * @return Builder with kie-server id set
     */
    WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withKieServerId(String kieServerId);

    /**
     * Return setup builder with configure Workbench http hostname.
     *
     * @param http HTTP hostname for Workbench
     * @return Builder
     */
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpWorkbenchHostname(String http);

    /**
     * Return setup builder with configure Workbench https hostname.
     *
     * @param https HTTPS hostname for Workbench
     * @return Builder
     */
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpsWorkbenchHostname(String https);

    /**
     * Return setup builder with configure Kie Server 1 http hostname.
     *
     * @param http HTTP hostname for Kie Server
     * @return Builder
     */
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpKieServer1Hostname(String http);

    /**
     * Return setup builder with configure Kie Server 1 https hostname.
     *
     * @param https HTTPS hostname for Kie Server
     * @return Builder
     */
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpsKieServer1Hostname(String https);

    /**
     * Return setup builder with configure Kie Server 2 http hostname.
     *
     * @param http HTTP hostname for Kie Server
     * @return Builder
     */
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpKieServer2Hostname(String http);

    /**
     * Return setup builder with configure Kie Server 2 https hostname.
     *
     * @param https HTTPS hostname for Kie Server
     * @return Builder
     */
    public WorkbenchRuntimeSmartRouterKieServerDatabaseSSOScenarioBuilder withHttpsKieServer2Hostname(String https);
}

/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.settings.LdapSettings;

/**
 * Cloud builder for clustered Workbench runtime, Smart router and two Kie Servers with two databases in project. Built setup
 * for ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario
 * @see ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario
 */
public interface ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder extends DeploymentScenarioBuilder<ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     *
     * Parameters will be used automatically
     *
     * @return Builder with configured internal maven repo.
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withInternalMavenRepo();

    /**
     * Return setup builder with specified Smart router id.
     * @param smartRouterId Smart router id.
     * @return Builder with configured Smart router id.
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId);

    /**
     * Return setup builder with specified timer service data store refresh interval.
     * @param timerServiceDataStoreRefreshInterval timer service data store refresh interval.
     * @return Builder with configured timer service data store refresh interval.
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder deploySso();

    /**
     * Return setup builder with an external LDAP.
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalLdap(LdapSettings ldapSettings);

    /**
     * Return setup builder with additional configuration of internal ldap.
     *
     * Parameters will be used automatically
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder with configured internal ldap.
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withInternalLdap(LdapSettings ldapSettings);

    /**
     * Return setup builder with configure Workbench http hostname.
     *
     * @param hostname HTTP hostname for Workbench
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Workbench https hostname.
     *
     * @param hostname HTTPS hostname for Workbench
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 1 http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer1Hostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 1 https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer1Hostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 2 http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpKieServer2Hostname(String hostname);

    /**
     * Return setup builder with configure Kie Server 2 https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withHttpsKieServer2Hostname(String hostname);
}

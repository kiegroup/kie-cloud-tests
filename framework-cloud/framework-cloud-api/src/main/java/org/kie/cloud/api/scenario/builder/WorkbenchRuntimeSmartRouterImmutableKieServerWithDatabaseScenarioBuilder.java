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

package org.kie.cloud.api.scenario.builder;

import java.time.Duration;

import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;

public interface WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder extends
                                                                                          KieDeploymentScenarioBuilder<WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder, WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     *
     * Parameters will be used automatically
     *
     * @return Builder with configured internal maven repo.
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withInternalMavenRepo();

    /**
     * @param kieServerId kie-server id
     * @return Builder with environment variable for kie-server id set to given id
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withKieServerId(String kieServerId);

    /**
     * Return configured builder with Kie Container deployment.
     *
     * @param kieContainerDeployment Kie Container deployment.
     * @return Builder with configured Kie container deployment
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withContainerDeployment(String kieContainerDeployment);

    /**
     * Return configured builder with Source location
     *
     * @param gitReference Repository reference (branch/tag). E.g. 'master'.
     * @param gitContextDir Repository context (location of pom file).
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withSourceLocation(String gitReference, String gitContextDir);

    /**
     * Return configured builder with Source location
     *
     * @param gitReference Repository reference (branch/tag). E.g. 'master'.
     * @param gitContextDir Repository context (location of pom file).
     * @param artifactDirs Directories containing built kjars, separated by
     * commas. For example "usertask-project/target,signaltask-project/target".
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withSourceLocation(String gitReference, String gitContextDir, String artifactDirs);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder deploySso();

    /**
     * Return setup builder with configure Kie Server http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withHttpKieServerHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withHttpsKieServerHostname(String hostname);

    /**
     * Return configured builder with enabled drools classes filter for Kie
     * server.
     *
     * @param droolsFilter set to true to enable drools classes filter.
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withDroolsServerFilterClasses(boolean droolsFilter);

    /**
     * Return setup builder with additional configuration of internal ldap.
     *
     * Parameters will be used automatically
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder with configured internal ldap.
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withLdap(LdapSettings ldapSettings);

    /**
     * Return setup builder with specified timer service data store refresh interval.
     *
     * @param timerServiceDataStoreRefreshInterval timer service data store refresh interval.
     * @return Builder with configured timer service data store refresh interval.
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withTimerServiceDataStoreRefreshInterval(Duration timerServiceDataStoreRefreshInterval);

    /**
     * Return setup builder with specified memory limit.
     *
     * @param limit memory limit (e.g.: 4Gi, etc).
     * @return Builder with configured memory limit.
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withKieServerMemoryLimit(String limit);

    /**
     * Return setup builder with additional GIT settings.
     *
     * @param gitSettings settings configuration of GIT
     * @return Builder
     */
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder withGitSettings(GitSettings gitSettings);
}

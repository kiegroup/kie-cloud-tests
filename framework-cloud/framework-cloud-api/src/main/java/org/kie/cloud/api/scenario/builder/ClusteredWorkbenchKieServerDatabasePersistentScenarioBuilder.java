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

import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.settings.LdapSettings;

public interface ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder extends DeploymentScenarioBuilder<ClusteredWorkbenchKieServerDatabasePersistentScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     *
     * Parameters will be used automatically
     *
     * @return Builder with configured internal maven repo.
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withInternalMavenRepo();

    /**
     * Return setup builder with configured Git hooks dir.
     * @param dir GIT_HOOKS_DIR
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withGitHooksDir(String dir);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder deploySso();

    /**
    *
    * Return setup builder with an external LDAP.
    *
    * @param ldapSettings configuration of LDAP represented by a class.
    * @return Builder
    */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withExternalLdap(LdapSettings ldapSettings);

    /**
    * Return setup builder with additional configuration of internal ldap.
    *
    * Parameters will be used automatically
    *
    * @param ldapSettings configuration of LDAP represented by a class.
    * @return Builder with configured internal ldap.
    */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withInternalLdap(LdapSettings ldapSettings);

    /**
     * Return setup builder with configure Workbench http hostname.
     *
     * @param hostname HTTP hostname for Workbench
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Workbench https hostname.
     *
     * @param hostname HTTPS hostname for Workbench
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpKieServerHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withHttpsKieServerHostname(String hostname);

    /**
     * Return setup builder with specified memory limit.
     *
     * @param limit memory limit (e.g.: 4Gi, etc).
     * @return Builder with configured memory limit.
     */
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder withWorkbenchMemoryLimit(String limit);
}

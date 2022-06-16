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

import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;

public interface WorkbenchKieServerPersistentScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchKieServerPersistentScenario> {
    /**
     * Return setup builder with additional configuration of internal maven repo.
     *
     * Parameters will be used automatically
     *
     * @return Builder with configured internal maven repo.
     */
    WorkbenchKieServerPersistentScenarioBuilder withInternalMavenRepo();

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder deploySso();

    /**
     * Return setup builder with additional GIT settings.
     *
     * @param gitSettings settings configuration of GIT
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withGitSettings(GitSettings gitSettings);

    /**
     * @param kieServerId kie-server id
     * @return Builder with kie-server id set
     */
    WorkbenchKieServerPersistentScenarioBuilder withKieServerId(String kieServerId);

    /**
     * Return setup builder with configure Workbench http hostname.
     *
     * @param hostname HTTP hostname for Workbench
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withHttpWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Workbench https hostname.
     *
     * @param hostname HTTPS hostname for Workbench
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withHttpsWorkbenchHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withHttpKieServerHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withHttpsKieServerHostname(String hostname);

    /**
     * Return setup builder with additional configuration of internal ldap.
     *
     * Parameters will be used automatically
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder with configured internal ldap.
     */
    WorkbenchKieServerPersistentScenarioBuilder withLdap(LdapSettings ldapSettings);

    /**
     * Return setup builder with additional configuration for RoleMapper. 
     * This can be used to mapped custom roles from ldap to roles needed 
     * for Kie Server or Workbench.
     *
     * @param rolesProperties When present, the RoleMapping will be 
     *  configured to use the provided properties file or roles with the 
     * following pattern 'role=role1;another-role=role2'. 
     * @param rolesKeepMapped When set to 'true' the mapped roles will 
     *  retain all roles, that have defined mappings.
     * @param rolesKeepNonMapped When set to 'true' the mapped roles will
     *  retain all roles, that have no defined mappings.
     * @return Builder with configured role mapper.
     */
    WorkbenchKieServerPersistentScenarioBuilder withRoleMapper(String rolesProperties, Boolean rolesKeepMapped, Boolean rolesKeepNonMapped);

    /**
     * Return setup builder with configured Git hooks dir.
     *
     * @param dir GIT_HOOKS_DIR
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withGitHooksDir(String dir);

    /**
     * KIE_SERVER_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE: If OpenShift integration of Business Central is turned on, call this method to set this parameter to true and enables
     * connection to KIE Server via an OpenShift internal Service endpoint.
     * (Sets the org.kie.server.controller.openshift.prefer.kieserver.service system property)
     *
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder usePublicIpAddress();

    /**
     * Persists the maven repositories of KieServers in PVs
     * @return Builder with Kie server repositories persistence.
     */
    WorkbenchKieServerPersistentScenarioBuilder withReposPersistence();

    /**
     * Admin user is stored in secret and deployments using this secret instead of
     * properties with name and password. Only for Operator.
     * 
     * @return Builder with configured secret admin credentials.
     */
    WorkbenchKieServerPersistentScenarioBuilder withSecretAdminCredentials();

    /**
     * Routes are having enable TLS Edge termination. If this is enabled, routes are
     * encrypted to OCP Router and then in OCP network the communication is not
     * encrypted. Only for Operator.
     * 
     * @return Builder with configured edge termination
     */
    WorkbenchKieServerPersistentScenarioBuilder withEnabledEdgeTermination();

}

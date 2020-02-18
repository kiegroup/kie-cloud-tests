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
     *
     * Return setup builder with configured LDAP.
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder withLdapSettings(LdapSettings ldapSettings);

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

}

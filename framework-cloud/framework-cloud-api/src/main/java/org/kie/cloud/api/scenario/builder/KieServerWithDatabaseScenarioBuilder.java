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

import org.kie.cloud.api.scenario.KieServerWithDatabaseScenario;
import org.kie.cloud.api.settings.LdapSettings;

public interface KieServerWithDatabaseScenarioBuilder extends KieDeploymentScenarioBuilder<KieServerWithDatabaseScenarioBuilder, KieServerWithDatabaseScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     *
     * Parameters will be used automatically
     *
     * @param waitForRunning If true, wait for maven repository to be fully running
     *
     * @return Builder with configured internal maven repo.
     */
    KieServerWithDatabaseScenarioBuilder withInternalMavenRepo(boolean waitForRunning);

    /**
     * @param kieServerId kie-server id
     * @return Builder with environment variable for kie-server id set to given id
     */
    KieServerWithDatabaseScenarioBuilder withKieServerId(String kieServerId);

    /**
     * Return configured builder with Kie Container deployment.
     *
     * @param kieContainerDeployment Kie Container deployment.
     * @return Builder with configured Kie container deployment
     */
    KieServerWithDatabaseScenarioBuilder withContainerDeployment(String kieContainerDeployment);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    KieServerWithDatabaseScenarioBuilder deploySso();

    /**
     * Return setup builder with configure Kie Server http hostname.
     *
     * @param hostname HTTP hostname for Kie Server
     * @return Builder
     */
    KieServerWithDatabaseScenarioBuilder withHttpKieServerHostname(String hostname);

    /**
     * Return setup builder with configure Kie Server https hostname.
     *
     * @param hostname HTTPS hostname for Kie Server
     * @return Builder
     */
    KieServerWithDatabaseScenarioBuilder withHttpsKieServerHostname(String hostname);

    /**
     * Return setup builder with an external LDAP.
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder
     */
    KieServerWithDatabaseScenarioBuilder withExternalLdap(LdapSettings ldapSettings);

    /**
     * Return setup builder with additional configuration of internal ldap.
     *
     * Parameters will be used automatically
     *
     * @param ldapSettings configuration of LDAP represented by a class.
     * @return Builder with configured internal ldap.
     */
    KieServerWithDatabaseScenarioBuilder withInternalLdap(LdapSettings ldapSettings);
}

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

public interface WorkbenchKieServerPersistentScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchKieServerPersistentScenario> {
    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo user name.
     * @param repoPassword Maven repo user password.
     * @return Builder with configured external maven repo.
     */
    WorkbenchKieServerPersistentScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * Return setup builder with additional configuration for SSO deployment.
     *
     * @return Builder
     */
    WorkbenchKieServerPersistentScenarioBuilder deploySso();

    /**
     * Return setup builder with Business Central user for the maven repository.
     *
     * @param user Business Central Maven repo user name.
     * @param password Business Central Maven repo user password.
     * @return Builder with configured Business Central Maven repo user.
     */
    WorkbenchKieServerPersistentScenarioBuilder withBusinessCentralMavenUser(String user, String password);

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
}

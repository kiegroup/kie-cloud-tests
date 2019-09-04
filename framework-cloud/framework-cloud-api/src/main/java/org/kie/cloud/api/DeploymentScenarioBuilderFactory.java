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

package org.kie.cloud.api;

import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.scenario.builder.EmployeeRosteringScenarioBuilder;
import org.kie.cloud.api.scenario.builder.HACepScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerAmqScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.builder.LdapSettingsBuilder;

public interface DeploymentScenarioBuilderFactory {
    String getCloudAPIImplementationName();

    WorkbenchKieServerScenarioBuilder getWorkbenchKieServerScenarioBuilder();
    WorkbenchKieServerPersistentScenarioBuilder getWorkbenchKieServerPersistentScenarioBuilder();
    ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder();
    ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder getClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder();
    KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder();
    KieServerScenarioBuilder getKieServerScenarioBuilder();
    KieServerWithDatabaseScenarioBuilder getKieServerWithMySqlScenarioBuilder();
    KieServerWithDatabaseScenarioBuilder getKieServerWithPostgreSqlScenarioBuilder();
    ImmutableKieServerScenarioBuilder getImmutableKieServerScenarioBuilder();
    ImmutableKieServerAmqScenarioBuilder getImmutableKieServerAmqScenarioBuilder();
    WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder();
    WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioBuilder();
    ClusteredWorkbenchKieServerPersistentScenarioBuilder getClusteredWorkbenchKieServerPersistentScenarioBuilder();
    ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder();

    LdapSettingsBuilder getLdapSettingsBuilder();
    EmployeeRosteringScenarioBuilder getEmployeeRosteringScenarioBuilder();

    HACepScenarioBuilder getHACepScenarioBuilder();

    void deleteNamespace(String namespace);
}

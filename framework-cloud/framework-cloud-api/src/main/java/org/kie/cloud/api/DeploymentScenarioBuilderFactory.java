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

import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerSettingsBuilder;
import org.kie.cloud.api.settings.builder.SmartRouterSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchSettingsBuilder;

public interface DeploymentScenarioBuilderFactory {
    String getCloudAPIImplementationName();

    WorkbenchKieServerScenarioBuilder getWorkbenchKieServerScenarioBuilder();
    WorkbenchKieServerDatabaseScenarioBuilder getWorkbenchKieServerDatabaseScenarioBuilder();
    WorkbenchKieServerDatabasePersistentScenarioBuilder getWorkbenchKieServerDatabasePersistentScenarioBuilder();
    WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder();
    KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder();
    GenericScenarioBuilder getGenericScenarioBuilder();

    KieServerSettingsBuilder getKieServerSettingsBuilder();
    KieServerS2ISettingsBuilder getKieServerHttpsS2ISettingsBuilder();
    KieServerS2ISettingsBuilder getKieServerBasicS2ISettingsBuilder();
    WorkbenchSettingsBuilder getWorkbenchSettingsBuilder();
    WorkbenchMonitoringSettingsBuilder getWorkbenchMonitoringSettingsBuilder();
    SmartRouterSettingsBuilder getSmartRouterSettingsBuilder();

    void deleteNamespace(String namespace);
}

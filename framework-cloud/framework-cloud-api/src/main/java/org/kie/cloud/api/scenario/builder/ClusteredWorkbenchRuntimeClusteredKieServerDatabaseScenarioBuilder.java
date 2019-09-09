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

import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario;

/**
 * Cloud builder for clustered Workbench runtime and clustered Kie Server with database in project. Built setup
 * for ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario
 * @see ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario
 */
public interface ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder extends DeploymentScenarioBuilder<ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario> {

    /**
     * Return setup builder with additional configuration of internal maven repo.
     * 
     * Parameters will be used automatically
     * 
     * @return Builder with configured internal maven repo.
     */
    ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder withInternalMavenRepo();
}

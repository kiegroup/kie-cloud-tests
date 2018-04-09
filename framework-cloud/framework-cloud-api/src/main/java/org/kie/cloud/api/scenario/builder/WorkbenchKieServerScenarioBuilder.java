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

import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;

/**
 * Cloud builder for Workbench and Kie Server in project. Built setup
 * for WorkbenchKieServerScenario
 * @see WorkbenchKieServerScenario
 */
public interface WorkbenchKieServerScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchKieServerScenario> {

    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     * @param repoUrl Maven repo URL.
     * @return Builder with configured external maven repo.
     */
    WorkbenchKieServerScenarioBuilder withExternalMavenRepo(String repoUrl);

    /**
     * @param kieServerId kie-server id
     * @return Builder with kie-server id environment variable set to given id
     */
    WorkbenchKieServerScenarioBuilder withKieServerId(String kieServerId);
}

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

import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;

/**
 * Cloud builder for Workbench runtime, Smart router and two Kie Servers with two databases in project. Built setup
 * for WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario
 * @see WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario
 */
public interface WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder extends DeploymentScenarioBuilder<WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario> {

    /**
     * Return setup builder with additional configuration of external maven
     * repo.
     * @param repoUrl Maven repo URL.
     * @param repoUserName Maven repo user name.
     * @param repoPassword Maven repo user password.
     * @return Builder with configured external maven repo.
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);

    /**
     * Return setup builder with specified Smart router id.
     * @param smartRouterId Smart router id.
     * @return Builder with configured Smart router id.
     */
    WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder withSmartRouterId(String smartRouterId);
}

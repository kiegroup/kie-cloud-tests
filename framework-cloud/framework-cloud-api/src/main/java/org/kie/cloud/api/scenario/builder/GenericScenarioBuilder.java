/*
 * Copyright 2017 JBoss by Red Hat.
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

import org.kie.cloud.api.scenario.GenericScenario;

public interface GenericScenarioBuilder extends DeploymentScenarioBuilder<GenericScenario> {

    /**
     * Return setup builder with configuration for Kie Server S2I.
     *
     * @param kieContainerDeployment KIE Server Container deployment
     * configuration in format:
     * containerId=groupId:artifactId:version|c2=g2:a2:v2
     * @param gitRepoUrl Git Repository URL.
     * @param gitReference Git branch/tag reference.
     * @param gitContextDir Path within Git project to build; empty for root
     * project directory.
     * @return Builder with configured Kie Server S2I.
     */
    GenericScenarioBuilder withKieServerS2I(String kieContainerDeployment, String gitRepoUrl, String gitReference, String gitContextDir);

    /**
     * Return setup builder with configuration for Kie Server S2I.
     *
     * @param managedMode Set to true to connect Kie Server to Controller.
     * @param connectToSmartRouter Set to true to connect Kie Server to Smart router,
     * @param kieContainerDeployment KIE Server Container deployment
     * configuration in format:
     * containerId=groupId:artifactId:version|c2=g2:a2:v2
     * @param gitRepoUrl Git Repository URL.
     * @param gitReference Git branch/tag reference.
     * @param gitContextDir Path within Git project to build; empty for root
     * project directory.
     * @return Builder with configured Kie Server S2I.
     */
    GenericScenarioBuilder withKieServerS2I(boolean managedMode, boolean connectToSmartRouter, String kieContainerDeployment, String gitRepoUrl, String gitReference, String gitContextDir);

    /**
     * Return setup builder with configured external datavase for Kie Server.
     *
     * @return Builder with configured external DB for the Kie server.
     */
    GenericScenarioBuilder withExternalDatabaseForKieServer();

    /**
     * Return setup builder with configuration for Workbench.
     *
     * @return Builder with configured Workbench,
     */
    GenericScenarioBuilder withWorkbench();
}

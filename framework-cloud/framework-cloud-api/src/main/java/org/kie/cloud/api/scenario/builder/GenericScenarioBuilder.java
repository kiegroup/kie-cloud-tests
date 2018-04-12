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
import org.kie.cloud.api.settings.DeploymentSettings;

public interface GenericScenarioBuilder extends DeploymentScenarioBuilder<GenericScenario> {

    /**
     * Return scenario Builder with added Kie Server deployment into scenario.
     *
     * @param kieServerSettings deployment settinfs for Kie Server.
     * @return Builder
     */
    GenericScenarioBuilder withKieServer(DeploymentSettings kieServerSettings);

    /**
     * Return scenario Builder with added Kie Server deployment into scenario.
     *
     * @param kieServersSettings deployment settinfs for Kie Server.
     * @return Builder
     */
    GenericScenarioBuilder withKieServer(DeploymentSettings... kieServersSettings);

    /**
     * Return scenario Builder with added Workbench deployment into scenario.
     *
     * @param workbenchSettings
     * @return Builder
     */
    GenericScenarioBuilder withWorkbench(DeploymentSettings workbenchSettings);

    /**
     * Return scenario Builder with added Workbench monitoring deployment into
     * scenario.
     *
     * @param workbenchSettings
     * @return Builder
     */
    GenericScenarioBuilder withMonitoring(DeploymentSettings workbenchSettings);

    /**
     * Return scenario Builder with added Smart router deployment into scenario.
     *
     * @param smartRouterSettings
     * @return Builder
     */
    GenericScenarioBuilder withSmartRouter(DeploymentSettings smartRouterSettings);

    /**
     * Return scenario Builder with added Controller deployment into scenario.
     *
     * @param controllerSettings
     * @return Builder
     */
    GenericScenarioBuilder withController(DeploymentSettings controllerSettings);
}

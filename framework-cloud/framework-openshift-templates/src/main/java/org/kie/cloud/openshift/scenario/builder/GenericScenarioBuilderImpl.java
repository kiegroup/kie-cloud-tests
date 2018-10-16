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
package org.kie.cloud.openshift.scenario.builder;

import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.openshift.scenario.GenericScenarioImpl;
import org.kie.cloud.openshift.settings.GenericScenarioSettings;

public class GenericScenarioBuilderImpl implements GenericScenarioBuilder {

    GenericScenarioSettings scenarioSettings = new GenericScenarioSettings();

    @Override
    public GenericScenario build() {
        return new GenericScenarioImpl(scenarioSettings);
    }

    @Override
    public GenericScenarioBuilder withKieServer(DeploymentSettings kieServerSettings) {
        scenarioSettings.addKieServer(kieServerSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withKieServer(DeploymentSettings... kieServersSettings) {
        scenarioSettings.addKieServers(kieServersSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withWorkbench(DeploymentSettings workbenchSettings) {
        scenarioSettings.addWorkbench(workbenchSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withMonitoring(DeploymentSettings workbenchSettings) {
        scenarioSettings.addMonitoring(workbenchSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withSmartRouter(DeploymentSettings smartRouterSettings) {
        scenarioSettings.addSmartRouter(smartRouterSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withController(DeploymentSettings controllerSettings) {
        scenarioSettings.addController(controllerSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withSso() {
        scenarioSettings.deploySso(true);
        return this;
    }

}

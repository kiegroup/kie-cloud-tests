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
package org.kie.cloud.openshift.scenario.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.openshift.scenario.GenericScenarioApb;

public class GenericScenarioBuilderApb implements GenericScenarioBuilder {

    private List<DeploymentSettings>
            kieServerSettingsList = new ArrayList<>(),
            workbenchSettingsList = new ArrayList<>(),
            monitoringSettingsList = new ArrayList<>(),
            smartRouterSettingsList = new ArrayList<>(),
            controllerSettingsList = new ArrayList<>();

    @Override
    public GenericScenario build() {
        return new GenericScenarioApb(kieServerSettingsList, workbenchSettingsList, monitoringSettingsList, smartRouterSettingsList, controllerSettingsList);
    }

    @Override
    public GenericScenarioBuilder withKieServer(DeploymentSettings kieServerSettings) {
        kieServerSettingsList.add(kieServerSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withKieServer(DeploymentSettings... kieServersSettings) {
        kieServerSettingsList.addAll(Arrays.asList(kieServersSettings));
        return this;
    }

    @Override
    public GenericScenarioBuilder withWorkbench(DeploymentSettings workbenchSettings) {
        workbenchSettingsList.add(workbenchSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withMonitoring(DeploymentSettings workbenchSettings) {
        monitoringSettingsList.add(workbenchSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withSmartRouter(DeploymentSettings smartRouterSettings) {
        smartRouterSettingsList.add(smartRouterSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withController(DeploymentSettings controllerSettings) {
        controllerSettingsList.add(controllerSettings);
        return this;
    }

    @Override
    public GenericScenarioBuilder withSso() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GenericScenarioBuilder withLdapSettings(LdapSettings ldapSettings) {
        throw new UnsupportedOperationException("Not supported yet.");
	}
}

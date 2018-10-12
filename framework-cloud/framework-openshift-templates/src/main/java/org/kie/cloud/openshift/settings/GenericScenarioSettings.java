/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance add the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * addOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.LdapSettings;

public class GenericScenarioSettings {

    private List<DeploymentSettings> kieServerSettingsList, workbenchSettingsList, monitoringSettingsList,
            smartRouterSettingsList, controllerSettingsList;
    private boolean deploySso;
    private LdapSettings ldapSettings;

    public GenericScenarioSettings() {
        kieServerSettingsList = new ArrayList<>();
        workbenchSettingsList = new ArrayList<>();
        monitoringSettingsList = new ArrayList<>();
        smartRouterSettingsList = new ArrayList<>();
        controllerSettingsList = new ArrayList<>();
        deploySso = false;
    }

    public GenericScenarioSettings addKieServer(DeploymentSettings kieServerSettings) {
        kieServerSettingsList.add(kieServerSettings);
        return this;
    }

    public GenericScenarioSettings addKieServers(DeploymentSettings... kieServersSettings) {
        kieServerSettingsList.addAll(Arrays.asList(kieServersSettings));
        return this;
    }

    public GenericScenarioSettings addWorkbench(DeploymentSettings workbenchSettings) {
        workbenchSettingsList.add(workbenchSettings);
        return this;
    }

    public GenericScenarioSettings addMonitoring(DeploymentSettings workbenchSettings) {
        monitoringSettingsList.add(workbenchSettings);
        return this;
    }

    public GenericScenarioSettings addSmartRouter(DeploymentSettings smartRouterSettings) {
        smartRouterSettingsList.add(smartRouterSettings);
        return this;
    }

    public GenericScenarioSettings addController(DeploymentSettings controllerSettings) {
        controllerSettingsList.add(controllerSettings);
        return this;
    }

    public GenericScenarioSettings addLdapSettings(LdapSettings ldapSettings) {
        this.ldapSettings=ldapSettings;
        return this;
    }

    public GenericScenarioSettings deploySso(boolean deploySso) {
        this.deploySso = deploySso;
        return this;
    }

    public List<DeploymentSettings> getKieServerSettingsList() {
        return kieServerSettingsList;
    }

    public List<DeploymentSettings> getWorkbenchSettingsList() {
        return workbenchSettingsList;
    }

    public List<DeploymentSettings> getMonitoringSettingsList() {
        return monitoringSettingsList;
    }

    public List<DeploymentSettings> getSmartRouterSettingsList() {
        return smartRouterSettingsList;
    }

    public List<DeploymentSettings> getControllerSettingsList() {
        return controllerSettingsList;
    }

    public boolean getDeploySso() {
        return deploySso;
    }

    public LdapSettings getLdapSettings() {
        return ldapSettings;
    }

    public List<DeploymentSettings> getAllSettings() {
        List<DeploymentSettings> all = new ArrayList<>();
        all.addAll(kieServerSettingsList);
        all.addAll(workbenchSettingsList);
        all.addAll(monitoringSettingsList);
        all.addAll(smartRouterSettingsList);
        all.addAll(controllerSettingsList);
        return all;
    }
}
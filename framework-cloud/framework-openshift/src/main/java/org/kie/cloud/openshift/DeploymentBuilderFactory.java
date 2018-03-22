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

package org.kie.cloud.openshift;

import cz.xtf.openshift.OpenShiftUtil;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerSettingsBuilder;
import org.kie.cloud.api.settings.builder.SmartRouterSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchSettingsBuilder;
import org.kie.cloud.openshift.scenario.builder.GenericScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.KieServerWithExternalDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerDatabasePersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerBasicS2ISettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerHttpsS2ISettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.SmartRouterSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.WorkbenchMonitoringSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.WorkbenchSettingsBuilderImpl;

public class DeploymentBuilderFactory implements DeploymentScenarioBuilderFactory {

    private static final String CLOUD_API_IMPLEMENTATION_NAME = "openshift";

    public DeploymentBuilderFactory() {
    }

    @Override public String getCloudAPIImplementationName() {
        return CLOUD_API_IMPLEMENTATION_NAME;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder getWorkbenchKieServerScenarioBuilder() {
        return new WorkbenchKieServerScenarioBuilderImpl();
    }

    @Override
    public WorkbenchKieServerDatabaseScenarioBuilder getWorkbenchKieServerDatabaseScenarioBuilder() {
        return new WorkbenchKieServerDatabaseScenarioBuilderImpl();
    }

    @Override
    public WorkbenchKieServerDatabasePersistentScenarioBuilder getWorkbenchKieServerDatabasePersistentScenarioBuilder() {
        return new WorkbenchKieServerDatabasePersistentScenarioBuilderImpl();
    }

    @Override
    public WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder() {
        return new WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl();
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder() {
        return new KieServerWithExternalDatabaseScenarioBuilderImpl();
    }

    @Override
    public GenericScenarioBuilder getGenericScenarioBuilder() {
        return new GenericScenarioBuilderImpl();
    }

    @Override
    public KieServerSettingsBuilder getKieServerSettingsBuilder() {
        return new KieServerSettingsBuilderImpl();
    }

    @Override
    public KieServerS2ISettingsBuilder getKieServerHttpsS2ISettingsBuilder() {
        return new KieServerHttpsS2ISettingsBuilderImpl();
    }

    @Override
    public KieServerS2ISettingsBuilder getKieServerBasicS2ISettingsBuilder() {
        return new KieServerBasicS2ISettingsBuilderImpl();
    }

    @Override
    public WorkbenchSettingsBuilder getWorkbenchSettingsBuilder() {
        return new WorkbenchSettingsBuilderImpl();
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder getWorkbenchMonitoringSettingsBuilder() {
        return new WorkbenchMonitoringSettingsBuilderImpl();
    }

    @Override
    public SmartRouterSettingsBuilder getSmartRouterSettingsBuilder() {
        return new SmartRouterSettingsBuilderImpl();
    }

    @Override
    public void deleteNamespace(String namespace) {
        try (OpenShiftUtil util = OpenShiftController.getOpenShiftUtil(namespace)) {
            util.deleteProject();
        }
    }
}

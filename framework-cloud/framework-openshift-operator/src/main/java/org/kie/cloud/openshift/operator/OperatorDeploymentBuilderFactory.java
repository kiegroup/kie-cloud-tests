/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.operator;

import cz.xtf.openshift.OpenShiftUtil;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.settings.builder.ControllerSettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerSettingsBuilder;
import org.kie.cloud.api.settings.builder.LdapSettingsBuilder;
import org.kie.cloud.api.settings.builder.SmartRouterSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchSettingsBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchKieServerScenarioBuilderImpl;

public class OperatorDeploymentBuilderFactory implements DeploymentScenarioBuilderFactory {

    private static final String CLOUD_API_IMPLEMENTATION_NAME = "openshift-operator";

    public OperatorDeploymentBuilderFactory() {
    }

    @Override
    public String getCloudAPIImplementationName() {
        return CLOUD_API_IMPLEMENTATION_NAME;
    }

    @Override
    public WorkbenchKieServerScenarioBuilder getWorkbenchKieServerScenarioBuilder() {
        return new WorkbenchKieServerScenarioBuilderImpl();
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder getWorkbenchKieServerPersistentScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder getWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerWithDatabaseScenarioBuilder getKieServerWithMySqlScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerWithDatabaseScenarioBuilder getKieServerWithPostgreSqlScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder getClusteredWorkbenchKieServerPersistentScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public GenericScenarioBuilder getGenericScenarioBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerSettingsBuilder getKieServerSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerSettingsBuilder getKieServerMySqlSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerSettingsBuilder getKieServerPostgreSqlSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ControllerSettingsBuilder getControllerSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public KieServerS2ISettingsBuilder getKieServerHttpsS2ISettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchSettingsBuilder getWorkbenchSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder getWorkbenchMonitoringSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SmartRouterSettingsBuilder getSmartRouterSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public LdapSettingsBuilder getLdapSettingsBuilder() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteNamespace(String namespace) {
        try (OpenShiftUtil util = OpenShiftController.getOpenShiftUtil(namespace)) {
            util.deleteProject();
        }
    }
}

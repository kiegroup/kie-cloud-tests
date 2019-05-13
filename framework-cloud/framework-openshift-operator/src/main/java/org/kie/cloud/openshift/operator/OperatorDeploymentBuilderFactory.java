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

import cz.xtf.core.openshift.OpenShift;

import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.scenario.builder.EmployeeRosteringScenarioBuilder;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.scenario.builder.HACepScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.settings.builder.ControllerSettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2IAmqSettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerSettingsBuilder;
import org.kie.cloud.api.settings.builder.LdapSettingsBuilder;
import org.kie.cloud.api.settings.builder.SmartRouterSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchSettingsBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.operator.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchKieServerPersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchKieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.settings.builder.LdapSettingsOperatorBuilder;

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
        return new WorkbenchKieServerPersistentScenarioBuilderImpl();
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder() {
        return new ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl();
    }

    @Override
    public ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder getClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerWithDatabaseScenarioBuilder getKieServerWithMySqlScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerWithDatabaseScenarioBuilder getKieServerWithPostgreSqlScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder() {
        return new ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl();
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder getClusteredWorkbenchKieServerPersistentScenarioBuilder() {
        return new ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl();
    }

    @Override
    public GenericScenarioBuilder getGenericScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerSettingsBuilder getKieServerSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerSettingsBuilder getKieServerMySqlSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerSettingsBuilder getKieServerPostgreSqlSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ControllerSettingsBuilder getControllerSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerS2ISettingsBuilder getKieServerHttpsS2ISettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KieServerS2IAmqSettingsBuilder getKieServerS2IAmqSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WorkbenchSettingsBuilder getWorkbenchSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder getWorkbenchMonitoringSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SmartRouterSettingsBuilder getSmartRouterSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LdapSettingsBuilder getLdapSettingsBuilder() {
        return new LdapSettingsOperatorBuilder();
    }

    @Override
    public EmployeeRosteringScenarioBuilder getEmployeeRosteringScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HACepScenarioBuilder getHACepScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteNamespace(String namespace) {
        try (OpenShift openShift = OpenShiftController.getOpenShift(namespace)) {
            openShift.deleteProject();
        }
    }
}

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
import org.kie.cloud.openshift.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.EmployeeRosteringScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.GenericScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.HACepScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.KieServerWithExternalDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.KieServerWithMySqlScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.KieServerWithPostgreSqlScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerPersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.settings.builder.ControllerSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerHttpsS2ISettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerMySqlSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerPostgreSqlSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerS2IAmqSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.KieServerSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.LdapSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.SmartRouterSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.WorkbenchMonitoringSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.WorkbenchSettingsBuilderImpl;

public class TemplatesDeploymentBuilderFactory implements DeploymentScenarioBuilderFactory {

    private static final String CLOUD_API_IMPLEMENTATION_NAME = "openshift-templates";

    public TemplatesDeploymentBuilderFactory() {
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
        return new ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilderImpl();
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder() {
        return new KieServerWithExternalDatabaseScenarioBuilderImpl();
    }

    @Override
    public KieServerWithDatabaseScenarioBuilder getKieServerWithMySqlScenarioBuilder() {
        return new KieServerWithMySqlScenarioBuilderImpl();
    }

    @Override
    public KieServerWithDatabaseScenarioBuilder getKieServerWithPostgreSqlScenarioBuilder() {
        return new KieServerWithPostgreSqlScenarioBuilderImpl();
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder getClusteredWorkbenchKieServerPersistentScenarioBuilder() {
        return new ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl();
    }

    @Override
    public ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder() {
        return new ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl();
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
    public KieServerSettingsBuilder getKieServerMySqlSettingsBuilder() {
        return new KieServerMySqlSettingsBuilderImpl();
    }

    @Override
    public KieServerSettingsBuilder getKieServerPostgreSqlSettingsBuilder() {
        return new KieServerPostgreSqlSettingsBuilderImpl();
    }

    @Override
    public ControllerSettingsBuilder getControllerSettingsBuilder() {
        return new ControllerSettingsBuilderImpl();
    }

    @Override
    public KieServerS2ISettingsBuilder getKieServerHttpsS2ISettingsBuilder() {
        return new KieServerHttpsS2ISettingsBuilderImpl();
    }

    @Override
    public KieServerS2IAmqSettingsBuilder getKieServerS2IAmqSettingsBuilder() {
        return new KieServerS2IAmqSettingsBuilderImpl();
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
    public LdapSettingsBuilder getLdapSettingsBuilder() {
        return new LdapSettingsBuilderImpl();
    }

    @Override
    public EmployeeRosteringScenarioBuilder getEmployeeRosteringScenarioBuilder() {
        return new EmployeeRosteringScenarioBuilderImpl();
    }

    @Override
    public HACepScenarioBuilder getHACepScenarioBuilder() {
        return new HACepScenarioBuilderImpl();
    }

    @Override
    public void deleteNamespace(String namespace) {
        try (OpenShift openShift = OpenShiftController.getOpenShift(namespace)) {
            openShift.deleteProject();
        }
    }
}

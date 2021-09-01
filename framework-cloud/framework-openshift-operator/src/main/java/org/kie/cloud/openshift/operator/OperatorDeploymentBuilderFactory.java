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
import org.kie.cloud.api.scenario.builder.HACepScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerAmqScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ImmutableKieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.settings.builder.LdapSettingsBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.operator.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.ImmutableKieServerAmqScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.ImmutableKieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.KieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.KieServerWithExternalDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.KieServerWithMySqlScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.KieServerWithPostgreSqlScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchKieServerPersistentScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchKieServerScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderImpl;
import org.kie.cloud.openshift.operator.scenario.builder.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilderImpl;
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
        return new KieServerWithExternalDatabaseScenarioBuilderImpl();
    }

    @Override
    public KieServerScenarioBuilder getKieServerScenarioBuilder() {
        return new KieServerScenarioBuilderImpl();
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
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder() {
        return new WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenarioBuilderImpl();
    }

    @Override
    public WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilder getWorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioBuilder() {
        return new WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenarioBuilderImpl();
    }

    @Override
    public ImmutableKieServerScenarioBuilder getImmutableKieServerScenarioBuilder() {
        return new ImmutableKieServerScenarioBuilderImpl();
    }

    @Override
    public ImmutableKieServerAmqScenarioBuilder getImmutableKieServerAmqScenarioBuilder() {
        return new ImmutableKieServerAmqScenarioBuilderImpl();
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

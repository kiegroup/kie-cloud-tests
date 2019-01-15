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
package org.kie.cloud.openshift;

import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder;
import org.kie.cloud.api.scenario.builder.GenericScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerPersistentScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.api.settings.builder.ControllerSettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.api.settings.builder.KieServerSettingsBuilder;
import org.kie.cloud.api.settings.builder.LdapSettingsBuilder;
import org.kie.cloud.api.settings.builder.SmartRouterSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.api.settings.builder.WorkbenchSettingsBuilder;
import org.kie.cloud.openshift.scenario.builder.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderApb;
import org.kie.cloud.openshift.scenario.builder.GenericScenarioBuilderApb;
import org.kie.cloud.openshift.scenario.builder.KieServerWithExternalDatabaseScenarioBuilderApb;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerPersistentScenarioBuilderApb;
import org.kie.cloud.openshift.scenario.builder.WorkbenchKieServerScenarioBuilderApb;
import org.kie.cloud.openshift.settings.builder.KieServerS2ISettingsBuilderApb;
import org.kie.cloud.openshift.settings.builder.LdapSettingsBuilderImpl;
import org.kie.cloud.openshift.settings.builder.WorkbenchMonitoringSettingsBuilderApb;

import cz.xtf.openshift.OpenShiftUtil;

public class ApbDeploymentBuilderFactory implements DeploymentScenarioBuilderFactory {

    private static final String CLOUD_API_IMPLEMENTATION_NAME = "openshift-apb";

    @Override
    public String getCloudAPIImplementationName() {
        return CLOUD_API_IMPLEMENTATION_NAME;
    }

    public ApbDeploymentBuilderFactory() {
    }

    @Override
    public WorkbenchKieServerScenarioBuilder getWorkbenchKieServerScenarioBuilder() {
        return new WorkbenchKieServerScenarioBuilderApb();
    }

    @Override
    public WorkbenchKieServerPersistentScenarioBuilder getWorkbenchKieServerPersistentScenarioBuilder() {
        return new WorkbenchKieServerPersistentScenarioBuilderApb();
    }

    @Override
    public ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder() {
        return new ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilderApb();
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder getKieServerWithExternalDatabaseScenarioBuilder() {
        return new KieServerWithExternalDatabaseScenarioBuilderApb();
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GenericScenarioBuilder getGenericScenarioBuilder() {
        return new GenericScenarioBuilderApb();
    }

    @Override
    public ControllerSettingsBuilder getControllerSettingsBuilder() {
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
    public KieServerS2ISettingsBuilder getKieServerHttpsS2ISettingsBuilder() {
        return new KieServerS2ISettingsBuilderApb();
    }

    @Override
    public WorkbenchSettingsBuilder getWorkbenchSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WorkbenchMonitoringSettingsBuilder getWorkbenchMonitoringSettingsBuilder() {
        return new WorkbenchMonitoringSettingsBuilderApb();
    }

    @Override
    public SmartRouterSettingsBuilder getSmartRouterSettingsBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LdapSettingsBuilder getLdapSettingsBuilder() {
        return new LdapSettingsBuilderImpl();
    }

    @Override
    public void deleteNamespace(String namespace) {
        try (OpenShiftUtil util = OpenShiftController.getOpenShiftUtil(namespace)) {
            util.deleteProject();
        }
    }

    @Override
    public ClusteredWorkbenchKieServerPersistentScenarioBuilder getClusteredWorkbenchKieServerPersistentScenarioBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

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
package org.kie.cloud.integrationtests.smoke;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.WorkbenchMonitoringSettingsBuilder;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;

@RunWith(Parameterized.class)
public class WorkbenchMonitoringScenarioIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<GenericScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public WorkbenchMonitoringSettingsBuilder workbenchMonitoringSettingsBuilder;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchMonitoringSettingsBuilder workbenchMonitoringSettingsBuilder = deploymentScenarioFactory.getWorkbenchMonitoringSettingsBuilder();

        return Arrays.asList(new Object[][]{
            //TODO: mainly for ABP
            {"Workbench monitoring + Smart router", workbenchMonitoringSettingsBuilder}
        });
    }

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        DeploymentSettings workbenchMonitoringSettings = workbenchMonitoringSettingsBuilder
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withMonitoring(workbenchMonitoringSettings)
                .build();
    }

    @Before
    public void setUp() {
        WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployments().get(0));
    }

    @After
    public void deleteRepo() {
    }

    @Test
    public void testWorkbenchHttps() {
        deploymentScenario.getWorkbenchDeployments().forEach(wbDeployment -> {
            HttpsWorkbenchTestProvider.testLoginScreen(wbDeployment, false);
            HttpsWorkbenchTestProvider.testControllerOperations(wbDeployment, false);
        });
    }

}

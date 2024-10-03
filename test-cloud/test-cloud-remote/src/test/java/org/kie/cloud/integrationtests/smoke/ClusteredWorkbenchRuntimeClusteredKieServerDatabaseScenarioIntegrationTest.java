/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.integrationtests.smoke;

import java.time.Duration;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.AutoScalerDeployment;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;

@Category({Smoke.class, JBPMOnly.class})
public class ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;
    private static HttpsWorkbenchTestProvider httpsWorkbenchTestProvider;

    private static final String HELLO_RULES_CONTAINER_ID = "helloRules";
    private static final String DEFINITION_PROJECT_CONTAINER_ID = "definition-project";
    private static final String CLOUDBALANCE_CONTAINER_ID = "cloudbalance";

    @BeforeClass
    public static void initializeDeployment() {
        try {
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioBuilder()
                    .withInternalMavenRepo()
                    .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }
        deploymentScenario.setLogFolderName(ClusteredWorkbenchRuntimeClusteredKieServerDatabaseScenarioIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
        httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        httpsWorkbenchTestProvider = HttpsWorkbenchTestProvider.create();

        // Workaround to speed test execution.
        // Create all containers while Kie servers are turned off to avoid expensive respins.
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

        deploymentScenario.getKieServerDeployment().setRouterTimeout(Duration.ofMinutes(3));
        AutoScalerDeployment.on(deploymentScenario.getKieServerDeployment(), () -> {
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), HELLO_RULES_CONTAINER_ID, "hello-rules-alias", Kjar.HELLO_RULES_SNAPSHOT, KieContainerStatus.STARTED);
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), DEFINITION_PROJECT_CONTAINER_ID, "definition-project-alias", Kjar.DEFINITION_SNAPSHOT,
                                             KieContainerStatus.STARTED);
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CLOUDBALANCE_CONTAINER_ID, "cloudbalance-alias", Kjar.CLOUD_BALANCE_SNAPSHOT, KieContainerStatus.STARTED);
        });
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testFireRules(deploymentScenario.getKieServerDeployment(), HELLO_RULES_CONTAINER_ID);
    }

    @Test
    public void testProcessFromMavenRepo() {
        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
    }

    @Test
    public void testKieServerHttps() {
        httpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerDeployment(), false);
    }

    @Test
    public void testWorkbenchHttps() {
        httpsWorkbenchTestProvider.testLoginScreen(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
        httpsWorkbenchTestProvider.testControllerOperations(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
    }
}

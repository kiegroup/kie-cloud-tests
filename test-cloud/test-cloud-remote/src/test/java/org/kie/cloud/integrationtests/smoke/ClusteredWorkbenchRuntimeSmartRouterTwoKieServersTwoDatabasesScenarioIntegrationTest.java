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

package org.kie.cloud.integrationtests.smoke;

import java.time.Duration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.MonitoringK8sFs;
import org.kie.cloud.integrationtests.category.OperatorNotSupported;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.SmartRouterTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.AutoScalerDeployment;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;

@Category({Smoke.class, JBPMOnly.class, MonitoringK8sFs.class})
public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;
    private static HttpsWorkbenchTestProvider httpsWorkbenchTestProvider;
    private static SmartRouterTestProvider smartRouterTestProvider;

    private static final String HELLO_RULES_CONTAINER_ID = "helloRules";
    private static final String DEFINITION_PROJECT_CONTAINER_ID = "definition-project";
    private static final String CLOUDBALANCE_CONTAINER_ID = "cloudbalance";

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                .withInternalMavenRepo()
                .build();
        deploymentScenario.setLogFolderName(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
        httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        httpsWorkbenchTestProvider = HttpsWorkbenchTestProvider.create();
        smartRouterTestProvider = SmartRouterTestProvider.create(deploymentScenario);

        // Workaround to speed test execution.
        // Create all containers while Kie servers are turned off to avoid expensive respins.
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());

        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
            KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

            kieServerDeployment.setRouterTimeout(Duration.ofMinutes(3));
            AutoScalerDeployment.on(kieServerDeployment, () -> {
                WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), HELLO_RULES_CONTAINER_ID, "hello-rules-alias", Kjar.HELLO_RULES_SNAPSHOT, KieContainerStatus.STARTED);
                WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), DEFINITION_PROJECT_CONTAINER_ID, "definition-project-alias", Kjar.DEFINITION_SNAPSHOT,
                                                 KieContainerStatus.STARTED);
                WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CLOUDBALANCE_CONTAINER_ID, "cloudbalance-alias", Kjar.CLOUD_BALANCE_SNAPSHOT,
                                                 KieContainerStatus.STARTED);
            });
        }

    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testFireRules(deploymentScenario.getKieServerOneDeployment(), HELLO_RULES_CONTAINER_ID);
        fireRulesTestProvider.testFireRules(deploymentScenario.getKieServerTwoDeployment(), HELLO_RULES_CONTAINER_ID);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerOneDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerTwoDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
    }

    @Test
    public void testKieServerHttps() {
        httpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerOneDeployment(), false);
        // Skipped as the check is too time consuming, the HTTPS functionality is verified by testKieServerInfo()
        // httpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerOneDeployment(), false);
        httpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerTwoDeployment(), false);
        // Skipped as the check is too time consuming, the HTTPS functionality is verified by testKieServerInfo()
        // httpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerTwoDeployment(), false);
    }

    @Test
    public void testWorkbenchHttps() {
        httpsWorkbenchTestProvider.testLoginScreen(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
        httpsWorkbenchTestProvider.testControllerOperations(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
    }

    @Test
    @Category({OperatorNotSupported.class}) //failing because of RHPAM-1561, skipping the test for Operator as Smart router doesn't support HTTPS in Kie server location yet, see RHPAM-2825
    public void testSmartRouter() {
        smartRouterTestProvider.testRouterLoadBalancing(deploymentScenario.getWorkbenchRuntimeDeployment(),
                                                        deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerOneDeployment(),
                                                        deploymentScenario.getKieServerTwoDeployment());
        smartRouterTestProvider.testRouterContainerIdLoadBalancing(deploymentScenario.getSmartRouterDeployment(),
                                                                   deploymentScenario.getKieServerOneDeployment(), deploymentScenario.getKieServerTwoDeployment());
        smartRouterTestProvider.testRouterContainerAliasLoadBalancing(deploymentScenario.getSmartRouterDeployment(),
                                                                      deploymentScenario.getKieServerOneDeployment(), deploymentScenario.getKieServerTwoDeployment());
    }
}

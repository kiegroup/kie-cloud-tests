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
import org.kie.cloud.integrationtests.category.ApbNotSupported;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.SmartRouterTestProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;

@Category({Smoke.class, JBPMOnly.class})
public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    private static final String HELLO_RULES_CONTAINER_ID = "helloRules";
    private static final String DEFINITION_PROJECT_CONTAINER_ID = "definition-project";
    private static final String CLOUDBALANCE_CONTAINER_ID = "cloudbalance";

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                                                      .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                                                      .build();
        deploymentScenario.setLogFolderName(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Workaround to speed test execution.
        // Create all containers while Kie servers are turned off to avoid expensive respins.
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());

        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
            KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

            kieServerDeployment.setRouterTimeout(Duration.ofMinutes(3));
            kieServerDeployment.scale(0);
            kieServerDeployment.waitForScale();

            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), HELLO_RULES_CONTAINER_ID, "hello-rules-alias", Kjar.HELLO_RULES_SNAPSHOT, KieContainerStatus.STARTED);
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), DEFINITION_PROJECT_CONTAINER_ID, "definition-project-alias", Kjar.DEFINITION_SNAPSHOT, KieContainerStatus.STARTED);
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CLOUDBALANCE_CONTAINER_ID, "cloudbalance-alias", Kjar.CLOUD_BALANCE_SNAPSHOT, KieContainerStatus.STARTED);
        }

        // Scaling to one is kept independently from for loop above and done in parallel, to speed up the test execution time
        deploymentScenario.getKieServerOneDeployment().scale(1);
        deploymentScenario.getKieServerTwoDeployment().scale(1);
        deploymentScenario.getKieServerOneDeployment().waitForScale();
        deploymentScenario.getKieServerTwoDeployment().waitForScale();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromExternalMavenRepo() {
        FireRulesTestProvider.testFireRules(deploymentScenario.getKieServerOneDeployment(), HELLO_RULES_CONTAINER_ID);
        FireRulesTestProvider.testFireRules(deploymentScenario.getKieServerTwoDeployment(), HELLO_RULES_CONTAINER_ID);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromExternalMavenRepo() {
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getKieServerOneDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getKieServerTwoDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
    }

    @Test
    public void testSolverFromExternalMavenRepo() throws Exception {
        OptaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerOneDeployment(), CLOUDBALANCE_CONTAINER_ID);
        OptaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerTwoDeployment(), CLOUDBALANCE_CONTAINER_ID);
    }

    @Test
    public void testKieServerHttps() {
        HttpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerOneDeployment(), false);
        // Skipped as the check is too time consuming, the HTTPS functionality is verified by testKieServerInfo()
        // HttpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerOneDeployment(), false);
        HttpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerTwoDeployment(), false);
        // Skipped as the check is too time consuming, the HTTPS functionality is verified by testKieServerInfo()
        // HttpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerTwoDeployment(), false);
    }

    @Test
    public void testWorkbenchHttps() {
        HttpsWorkbenchTestProvider.testLoginScreen(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
        HttpsWorkbenchTestProvider.testControllerOperations(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
    }

    @Test
    @Category(ApbNotSupported.class) //failing because of RHPAM-1561
    public void testSmartRouter() {
        SmartRouterTestProvider.testRouterLoadBalancing(deploymentScenario.getWorkbenchRuntimeDeployment(),
                deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerOneDeployment(),
                deploymentScenario.getKieServerTwoDeployment());
        SmartRouterTestProvider.testRouterContainerIdLoadBalancing(deploymentScenario.getSmartRouterDeployment(),
                deploymentScenario.getKieServerOneDeployment(), deploymentScenario.getKieServerTwoDeployment());
        SmartRouterTestProvider.testRouterContainerAliasLoadBalancing(deploymentScenario.getSmartRouterDeployment(),
                deploymentScenario.getKieServerOneDeployment(), deploymentScenario.getKieServerTwoDeployment());
    }
}

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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.MonitoringK8sFs;
import org.kie.cloud.integrationtests.category.OperatorNotSupported;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.SmartRouterTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

@Category({Smoke.class, JBPMOnly.class, MonitoringK8sFs.class})
public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static OptaplannerTestProvider optaplannerTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;
    private static HttpsWorkbenchTestProvider httpsWorkbenchTestProvider;
    private static SmartRouterTestProvider smartRouterTestProvider;

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
        optaplannerTestProvider = OptaplannerTestProvider.create(deploymentScenario);
        httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        httpsWorkbenchTestProvider = HttpsWorkbenchTestProvider.create();
        smartRouterTestProvider = SmartRouterTestProvider.create(deploymentScenario);

    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerOneDeployment());
        fireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerOneDeployment());
        processTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    public void testSolverFromMavenRepo() throws Exception {
        optaplannerTestProvider.testDeployFromKieServerAndExecuteSolver(deploymentScenario.getKieServerOneDeployment());
        optaplannerTestProvider.testDeployFromKieServerAndExecuteSolver(deploymentScenario.getKieServerTwoDeployment());
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

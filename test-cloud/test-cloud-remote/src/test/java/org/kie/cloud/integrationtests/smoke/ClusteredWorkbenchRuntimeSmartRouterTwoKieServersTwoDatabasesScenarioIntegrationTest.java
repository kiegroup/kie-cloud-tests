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
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.SmartRouterTestProvider;
import org.kie.cloud.integrationtests.util.ScenarioDeployer;
import org.kie.cloud.maven.constants.MavenConstants;

@Category(Smoke.class)
public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                                                      .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                                                      .build();
        deploymentScenario.setLogFolderName(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromExternalMavenRepo() {
        FireRulesTestProvider.testFireRules(deploymentScenario.getKieServerOneDeployment());
        FireRulesTestProvider.testFireRules(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromExternalMavenRepo() {
        ProcessTestProvider.testProcesses(deploymentScenario.getKieServerOneDeployment());
        ProcessTestProvider.testProcesses(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    public void testSolverFromExternalMavenRepo() {
        OptaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerOneDeployment());
        OptaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    public void testKieServerHttps() {
        HttpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerOneDeployment(), false);
        HttpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerOneDeployment(), false);
        HttpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerTwoDeployment(), false);
        HttpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerTwoDeployment(), false);
    }

    @Test
    public void testWorkbenchHttps() {
        HttpsWorkbenchTestProvider.testLoginScreen(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
        HttpsWorkbenchTestProvider.testControllerOperations(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
    }

    @Test
    public void testSmartRouter() {
        //TODO failing because of RHPAM-1561
        SmartRouterTestProvider.testRouterLoadBalancing(deploymentScenario.getWorkbenchRuntimeDeployment(),
                deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerOneDeployment(),
                deploymentScenario.getKieServerTwoDeployment());
        SmartRouterTestProvider.testRouterContainerIdLoadBalancing(deploymentScenario.getSmartRouterDeployment(),
                deploymentScenario.getKieServerOneDeployment(), deploymentScenario.getKieServerTwoDeployment());
        SmartRouterTestProvider.testRouterContainerAliasLoadBalancing(deploymentScenario.getSmartRouterDeployment(),
                deploymentScenario.getKieServerOneDeployment(), deploymentScenario.getKieServerTwoDeployment());
    }
}

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
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.smoke.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.smoke.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.smoke.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.util.ScenarioDeployer;
import org.kie.cloud.maven.constants.MavenConstants;

@Category(Smoke.class)
public class ClusteredWorkbenchKieServerDatabasePersistentScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchKieServerDatabasePersistentScenario deploymentScenario;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                                                      .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                                                      .build();
        deploymentScenario.setLogFolderName(ClusteredWorkbenchKieServerDatabasePersistentScenarioIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromExternalMavenRepo() {
        FireRulesTestProvider.testFireRules(deploymentScenario.getKieServerDeployment());
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromExternalMavenRepo() {
        ProcessTestProvider.testProcess(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testSolverFromExternalMavenRepo() {
        OptaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testDeployContainerFromWorkbench() {
        FireRulesTestProvider.testDeployFromWorkbenchAndFireRules(deploymentScenario.getWorkbenchDeployment(), deploymentScenario.getKieServerDeployment());
    }
}

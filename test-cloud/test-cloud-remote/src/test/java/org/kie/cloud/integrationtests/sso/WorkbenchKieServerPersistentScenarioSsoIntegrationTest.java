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
package org.kie.cloud.integrationtests.sso;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.integrationtests.category.Baseline;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.PersistenceTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProjectBuilderTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;

@Category(Baseline.class)
public class WorkbenchKieServerPersistentScenarioSsoIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String REPOSITORY_NAME = generateNameWithPrefix(WorkbenchKieServerPersistentScenarioSsoIntegrationTest.class.getSimpleName());

    private static WorkbenchKieServerScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;
    private static HttpsWorkbenchTestProvider httpsWorkbenchTestProvider;
    private static PersistenceTestProvider persistenceTestProvider;
    private static ProjectBuilderTestProvider projectBuilderTestProvider;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                                                      .deploySso()
                                                      .withInternalMavenRepo()
                                                      .withGitSettings(GitSettings.fromProperties()
                                                                       .withRepository(REPOSITORY_NAME,
                                                                                       WorkbenchKieServerPersistentScenarioSsoIntegrationTest.class.getResource(
                                                                                                                                              PROJECT_SOURCE_FOLDER + "/" + Kjar.HELLO_RULES.getArtifactName()).getFile()))
                                                      .build();

        deploymentScenario.setLogFolderName(WorkbenchKieServerPersistentScenarioSsoIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
        httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        httpsWorkbenchTestProvider = HttpsWorkbenchTestProvider.create();
        persistenceTestProvider = PersistenceTestProvider.create();
        projectBuilderTestProvider = ProjectBuilderTestProvider.create();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        persistenceTestProvider.testControllerPersistence(deploymentScenario);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testCreateAndDeployProject() {
        projectBuilderTestProvider.testCreateAndDeployProject(deploymentScenario.getWorkbenchDeployment(),
                                                              deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testDeployContainerFromWorkbench() {
        fireRulesTestProvider.testDeployFromWorkbenchAndFireRules(deploymentScenario.getWorkbenchDeployment(),
                                                                  deploymentScenario.getKieServerDeployment(),
                                                                  deploymentScenario.getGitProvider().getRepositoryUrl(REPOSITORY_NAME));
    }

    @Test
    public void testKieServerHttps() {
        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            httpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, true);
            httpsKieServerTestProvider.testDeployContainer(kieServerDeployment, true);
        }
    }

    @Test
    public void testWorkbenchHttps() {
        for (WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            httpsWorkbenchTestProvider.testLoginScreen(workbenchDeployment, true);
            httpsWorkbenchTestProvider.testControllerOperations(workbenchDeployment, true);
        }
    }
}

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
package org.kie.cloud.integrationtests.s2i;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.provider.git.Git;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;

@Category({JBPMOnly.class})
public class KieServerS2iJbpmIntegrationTest extends AbstractCloudIntegrationTest {

    private static WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario deploymentScenario;
    private static String repositoryName;

    private static final String CONTAINER_ALIAS_ID = "cont-alias";
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "(" + CONTAINER_ALIAS_ID + ")=" + Kjar.DEFINITION.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    @BeforeClass
    public static void initializeDeployment() {
        repositoryName = Git.getProvider().createGitRepositoryWithPrefix("KieServerS2iJbpmRepository", KieServerS2iJbpmIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        try {
            deploymentScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder()
                                                          .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                          .withSourceLocation(Git.getProvider().getRepositoryUrl(repositoryName), REPO_BRANCH, DEFINITION_PROJECT_NAME)
                                                          .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }

        deploymentScenario.setLogFolderName(KieServerS2iJbpmIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        Git.getProvider().deleteGitRepository(repositoryName);
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testContainerAfterExecServerS2IStart() {
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), CONTAINER_ALIAS_ID);
    }

    @Test
    public void testProcessUsingSmartRouter() {
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerDeployment(), CONTAINER_ALIAS_ID);
    }

    @Test
    public void testKieServerHttps() {
        HttpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerDeployment(), false);
    }

    @Test
    public void testWorkbenchHttps() {
        HttpsWorkbenchTestProvider.testLoginScreen(deploymentScenario.getWorkbenchRuntimeDeployment(), false);
    }
}

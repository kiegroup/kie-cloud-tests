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

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProjectBuilderTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;

public class ClusteredWorkbenchKieServerPersistentScenarioSsoIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String REPOSITORY_NAME = generateNameWithPrefix(ClusteredWorkbenchKieServerPersistentScenarioSsoIntegrationTest.class.getSimpleName());

    private static ClusteredWorkbenchKieServerDatabasePersistentScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static ProjectBuilderTestProvider projectBuilderTestProvider;

    private static final String SECURED_URL_PREFIX = "secured-";
    private static final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";
    private static final String BUSINESS_CENTRAL_NAME = "rhpamcentr";
    private static final String KIE_SERVER_NAME = "kieserver";
    private static final String BUSINESS_CENTRAL_HOSTNAME = BUSINESS_CENTRAL_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    @BeforeClass
    public static void initializeDeployment() {
        GitSettings gitSettings = GitSettings.fromProperties()
                                             .withRepository(REPOSITORY_NAME,
                                                             ClusteredWorkbenchKieServerPersistentScenarioSsoIntegrationTest.class.getResource(
                                                                                                                                               PROJECT_SOURCE_FOLDER + "/" + Kjar.HELLO_RULES.getArtifactName()).getFile());

        if (deploymentScenarioFactory.getCloudAPIImplementationName().equals("openshift-operator")) {
            try {
                deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                        .deploySso()
                        .withInternalMavenRepo()
                        .withGitSettings(gitSettings)
                        .build();
            } catch (UnsupportedOperationException ex) {
                Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
            }
        } else {
            try {
                deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                        .deploySso()
                        .withInternalMavenRepo()
                        .withGitSettings(gitSettings)
                        .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                        .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                        .withHttpKieServerHostname(RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                        .withHttpsKieServerHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                        .build();
            } catch (UnsupportedOperationException ex) {
                Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
            }
        }
        deploymentScenario.setLogFolderName(ClusteredWorkbenchKieServerPersistentScenarioSsoIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
        projectBuilderTestProvider = ProjectBuilderTestProvider.create();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
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
}

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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.smoke.WorkbenchKieServerPersistentScenarioIntegrationTest;
import org.kie.cloud.integrationtests.smoke.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.sso.testproviders.PersistenceTestProvider;
import org.kie.cloud.integrationtests.util.ScenarioDeployer;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.constants.MavenConstants;

public class AuthSsoIntegrationTest extends AbstractCloudIntegrationTest {

    private static WorkbenchKieServerScenario deploymentScenario;

    private static final String SECURED_URL_PREFIX = "secured-";
    private static final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";
    private static final String BUSINESS_CENTRAL_NAME = "rhpamcentr";
    private static final String KIE_SERVER_NAME = "kieserver";
    private static final String BUSINESS_CENTRAL_HOSTNAME = BUSINESS_CENTRAL_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private String repositoryName;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .deploySso()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpKieServerHostname(RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .withHttpsKieServerHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .build();
        deploymentScenario.setLogFolderName(WorkbenchKieServerPersistentScenarioIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Before
    public void setUp() {
        repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());
        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

    }

    @After
    public void tearDown() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        PersistenceTestProvider.testControllerPersistence(deploymentScenario);
    }

    @Test
    @Ignore("Ignored as the tests are affected by RHPAM-1361. Unignore when the JIRA will be fixed. https://issues.jboss.org/browse/RHPAM-1361")
    public void testMultipleDifferentProcessesOnSameKieServer() {
        ProcessTestProvider.testMultipleDifferentProcesses(deploymentScenario.getKieServerDeployment());
    }
}

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
package org.kie.cloud.integrationtests.ldap;

import java.time.Duration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.category.Baseline;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.PersistenceTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProjectBuilderTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.AutoScalerDeployment;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.LdapSettingsConstants;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;

@Category(Baseline.class)
public class WorkbenchKieServerPersistentScenarioLdapIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String REPOSITORY_NAME = generateNameWithPrefix(WorkbenchKieServerPersistentScenarioLdapIntegrationTest.class.getSimpleName());

    private static WorkbenchKieServerScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;
    private static HttpsWorkbenchTestProvider httpsWorkbenchTestProvider;
    private static PersistenceTestProvider persistenceTestProvider;
    private static ProjectBuilderTestProvider projectBuilderTestProvider;

    private static final String HELLO_RULES_CONTAINER_ID = "helloRules";
    private static final String DEFINITION_PROJECT_CONTAINER_ID = "definition-project";
    private static final String CLOUDBALANCE_CONTAINER_ID = "cloudbalance";

    @BeforeClass
    public static void initializeDeployment() {

        LdapSettings ldapSettings = deploymentScenarioFactory.getLdapSettingsBuilder()
                .withLdapBindDn(LdapSettingsConstants.BIND_DN)
                .withLdapBindCredential(LdapSettingsConstants.BIND_CREDENTIAL)
                .withLdapBaseCtxDn(LdapSettingsConstants.BASE_CTX_DN)
                .withLdapBaseFilter(LdapSettingsConstants.BASE_FILTER)
                .withLdapSearchScope(LdapSettingsConstants.SEARCH_SCOPE)
                .withLdapSearchTimeLimit(LdapSettingsConstants.SEARCH_TIME_LIMIT)
                .withLdapRoleAttributeId(LdapSettingsConstants.ROLE_ATTRIBUTE_ID)
                .withLdapRolesCtxDn(LdapSettingsConstants.ROLES_CTX_DN)
                .withLdapRoleFilter(LdapSettingsConstants.ROLE_FILTER)
                .withLdapRoleRecursion(LdapSettingsConstants.ROLE_RECURSION)
                .build();

        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                                  .withLdap(ldapSettings)
                                  .withInternalMavenRepo()
                                  .withGitSettings(GitSettings.fromProperties()
                                                              .withRepository(REPOSITORY_NAME,
                                                                              WorkbenchKieServerPersistentScenarioLdapIntegrationTest.class.getResource(
                                                                                                                                      PROJECT_SOURCE_FOLDER + "/" + Kjar.HELLO_RULES.getArtifactName()).getFile()))
                                  .build();
        deploymentScenario
                  .setLogFolderName(WorkbenchKieServerPersistentScenarioLdapIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
        httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        httpsWorkbenchTestProvider = HttpsWorkbenchTestProvider.create();
        persistenceTestProvider = PersistenceTestProvider.create();
        projectBuilderTestProvider = ProjectBuilderTestProvider.create();

        // Workaround to speed test execution.
        // Create all containers while Kie servers are turned off to avoid expensive respins.
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
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
    public void testWorkbenchControllerPersistence() {
        persistenceTestProvider.testControllerPersistence(deploymentScenario);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
    }

    @Test
    public void testCreateAndDeployProject() {
        projectBuilderTestProvider.testCreateAndDeployProject(deploymentScenario.getWorkbenchDeployment(),
                                                              deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testFireRules(deploymentScenario.getKieServerDeployment(), HELLO_RULES_CONTAINER_ID);
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
            httpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, false);
            // Skipped as the check is too time consuming, the HTTPS functionality is verified by testKieServerInfo()
            // httpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerDeployment(), false);
        }
    }

    @Test
    public void testWorkbenchHttps() {
        for (WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            httpsWorkbenchTestProvider.testLoginScreen(workbenchDeployment, false);
            httpsWorkbenchTestProvider.testControllerOperations(workbenchDeployment, false);
        }
    }
}

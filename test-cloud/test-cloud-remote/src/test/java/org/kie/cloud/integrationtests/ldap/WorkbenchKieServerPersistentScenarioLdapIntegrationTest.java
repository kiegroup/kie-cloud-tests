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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.category.Baseline;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.testproviders.PersistenceTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProjectBuilderTestProvider;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
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

    private static WorkbenchKieServerScenario deploymentScenario;

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
                .withLdapDefaultRole(LdapSettingsConstants.DEFAULT_ROLE).build();

        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .withLdapSettings(ldapSettings).withExternalMavenRepo(MavenConstants.getMavenRepoUrl(),
                        MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        deploymentScenario
                .setLogFolderName(WorkbenchKieServerPersistentScenarioLdapIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Workaround to speed test execution.
        // Create all containers while Kie servers are turned off to avoid expensive respins.
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

        deploymentScenario.getKieServerDeployment().scale(0);
        deploymentScenario.getKieServerDeployment().waitForScale();

        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), HELLO_RULES_CONTAINER_ID, "hello-rules-alias", Kjar.HELLO_RULES_SNAPSHOT, KieContainerStatus.STARTED);
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), DEFINITION_PROJECT_CONTAINER_ID, "definition-project-alias", Kjar.DEFINITION_SNAPSHOT, KieContainerStatus.STARTED);
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CLOUDBALANCE_CONTAINER_ID, "cloudbalance-alias", Kjar.CLOUD_BALANCE_SNAPSHOT, KieContainerStatus.STARTED);

        deploymentScenario.getKieServerDeployment().scale(1);
        deploymentScenario.getKieServerDeployment().waitForScale();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    @Ignore("Ignored as the tests are affected by RHPAM-1354. Unignore when the JIRA will be fixed. https://issues.jboss.org/browse/RHPAM-1354")
    public void testWorkbenchControllerPersistence() {
        PersistenceTestProvider.testControllerPersistence(deploymentScenario);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromExternalMavenRepo() {
        ProcessTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
    }

    @Test
    @Ignore("Ignored as the tests are affected by RHPAM-1544. Unignore when the JIRA will be fixed. https://issues.jboss.org/browse/RHPAM-1544")
    public void testCreateAndDeployProject() {
        ProjectBuilderTestProvider.testCreateAndDeployProject(deploymentScenario.getWorkbenchDeployment(),
                deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testRulesFromExternalMavenRepo() {
        FireRulesTestProvider.testFireRules(deploymentScenario.getKieServerDeployment(), HELLO_RULES_CONTAINER_ID);
    }

    @Test
    public void testSolverFromExternalMavenRepo() throws Exception {
        OptaplannerTestProvider.testExecuteSolver(deploymentScenario.getKieServerDeployment(), CLOUDBALANCE_CONTAINER_ID);
    }

    @Test
    public void testDeployContainerFromWorkbench() {
        FireRulesTestProvider.testDeployFromWorkbenchAndFireRules(deploymentScenario.getWorkbenchDeployment(),
                deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testKieServerHttps() {
        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            HttpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, false);
            // Skipped as the check is too time consuming, the HTTPS functionality is verified by testKieServerInfo()
            // HttpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerDeployment(), false);
        }
    }

    @Test
    public void testWorkbenchHttps() {
        for (WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            HttpsWorkbenchTestProvider.testLoginScreen(workbenchDeployment, false);
            HttpsWorkbenchTestProvider.testControllerOperations(workbenchDeployment, false);
        }
    }
}

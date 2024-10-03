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
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProjectBuilderTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.LdapSettingsConstants;

public class ClusteredWorkbenchKieServerPersistentScenarioLdapIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String REPOSITORY_NAME = generateNameWithPrefix(ClusteredWorkbenchKieServerDatabasePersistentScenario.class.getSimpleName());

    private static ClusteredWorkbenchKieServerDatabasePersistentScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static ProjectBuilderTestProvider projectBuilderTestProvider;

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

        try {
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                    .withLdap(ldapSettings)
                    .withInternalMavenRepo()
                    .withGitSettings(GitSettings.fromProperties()
                                     .withRepository(REPOSITORY_NAME,
                                                     ClusteredWorkbenchKieServerDatabasePersistentScenario.class.getResource(
                                                                                                           PROJECT_SOURCE_FOLDER + "/" + Kjar.HELLO_RULES.getArtifactName()).getFile()))
                    .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }

        deploymentScenario.setLogFolderName(ClusteredWorkbenchKieServerPersistentScenarioLdapIntegrationTest.class.getSimpleName());
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

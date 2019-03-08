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

package org.kie.cloud.integrationtests.ldap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.OptaplannerTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.integrationtests.util.LdapSettingsConstants;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioLdapIntegrationTest extends AbstractCloudIntegrationTest {

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

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
                .withLdapDefaultRole(LdapSettingsConstants.DEFAULT_ROLE)
                .build();

        deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
        .withLdapSettings(ldapSettings)
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        deploymentScenario.setLogFolderName(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioLdapIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromExternalMavenRepo() {
        FireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerOneDeployment());
        FireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromExternalMavenRepo() {
        ProcessTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerOneDeployment());
        ProcessTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    public void testSolverFromExternalMavenRepo() {
        OptaplannerTestProvider.testDeployFromKieServerAndExecuteSolver(deploymentScenario.getKieServerOneDeployment());
        OptaplannerTestProvider.testDeployFromKieServerAndExecuteSolver(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    public void testKieServerHttps() {
        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            HttpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, false);
            HttpsKieServerTestProvider.testDeployContainer(kieServerDeployment, false);
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

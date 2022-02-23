/*
 * Copyright 2022 JBoss by Red Hat.
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
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.api.settings.LdapSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.TemplateNotSupported;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
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

@Category(TemplateNotSupported.class)
public class WorkbenchKieServerPersistentScenarioDefaultUserLdapIntegrationTest  extends AbstractCloudIntegrationTest {

    private static WorkbenchKieServerScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;

    private static final String HELLO_RULES_CONTAINER_ID = "helloRules";
    private static final String DEFINITION_PROJECT_CONTAINER_ID = "definition-project";

    private static final String DEFAULT_APP_USER = "default-"+DeploymentConstants.getAppUser();

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
                .withLdapDefaultRole(LdapSettingsConstants.KIE_SERVER_DEFAULT_ROLE)
                .build();

        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                                  .withLdap(ldapSettings)
                                  .withInternalMavenRepo()
                                  .build();
        deploymentScenario
                  .setLogFolderName(WorkbenchKieServerPersistentScenarioDefaultUserLdapIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);

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
        });

        // Change user in deployment scenario to use the one with mapped roles
        deploymentScenario.getKieServerDeployment().setUsername(DEFAULT_APP_USER);
        deploymentScenario.getWorkbenchDeployment().setUsername(DEFAULT_APP_USER);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepoWithDefaultUser() {
        processTestProvider.testExecuteProcessWithSignal(deploymentScenario.getKieServerDeployment(), DEFINITION_PROJECT_CONTAINER_ID);
    }

    @Test
    public void testRulesFromMavenRepoWithDefaultUser() {
        fireRulesTestProvider.testFireRules(deploymentScenario.getKieServerDeployment(), HELLO_RULES_CONTAINER_ID);
    }

}

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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.scenario.KieServerWithDatabaseScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(Smoke.class)
@RunWith(Parameterized.class)
public class KieServerWithBuiltKjarIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<DeploymentScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public DeploymentScenario kieServerScenario;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithBuiltKjarIntegrationTest.class);

    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "defaultKieSession";
    private static final String HELLO_RULE = "Hello.";
    private static final String WORLD_RULE = "World.";

    private static final Kjar DEPLOYED_KJAR = Kjar.HELLO_RULES_SNAPSHOT;
    private static final ReleaseId RELEASE_ID = new ReleaseId(DEPLOYED_KJAR.getGroupId(), DEPLOYED_KJAR.getName(), DEPLOYED_KJAR.getVersion());
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + DEPLOYED_KJAR.toString();

    private static KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private KieServicesClient kieServerClient;
    private RuleServicesClient ruleClient;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            KieServerWithDatabaseScenario kieServerMySqlScenario = deploymentScenarioFactory.getKieServerWithMySqlScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .build();
            scenarios.add(new Object[] { "KIE Server + MySQL", kieServerMySqlScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server + MySQL is skipped.", ex);
        }

        try {
            KieServerWithDatabaseScenario kieServerPostgreSqlScenario = deploymentScenarioFactory.getKieServerWithPostgreSqlScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .build();
            scenarios.add(new Object[] { "KIE Server + PostgreSQL", kieServerPostgreSqlScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server + PostgreSQL is skipped.", ex);
        }

        try {
            DeploymentSettings kieServerSettings = deploymentScenarioFactory.getKieServerSettingsBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .build();
            GenericScenario kieServerScenario = deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerSettings)
                .build();
            scenarios.add(new Object[] { "KIE Server", kieServerScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server is skipped.", ex);
        }

        return scenarios;
    }

    @Override
    protected DeploymentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return kieServerScenario;
    }

    @BeforeClass
    public static void deployMavenProject() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/hello-rules-snapshot").getFile());
    }

    @Before
    public void initializeClients() {
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        ruleClient = KieServerClientProvider.getRuleClient(deploymentScenario.getKieServerDeployments().get(0));
    }

    @Test
    public void testRulesFromExternalMavenRepo() {
        ServiceResponse<KieContainerResource> containerInfo = kieServerClient.getContainerInfo(CONTAINER_ID);
        KieServerAssert.assertSuccess(containerInfo);
        assertThat(containerInfo.getResult().getReleaseId()).isEqualTo(RELEASE_ID);

        List<Command<?>> commands = new ArrayList<>();
        BatchExecutionCommand batchExecutionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);
        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecutionCommand);

        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();
        assertThat(result).isNotNull();
        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertThat(outcome).hasSize(2);
        assertThat(outcome.get(0)).startsWith(HELLO_RULE);
        assertThat(outcome.get(1)).startsWith(WORLD_RULE);
    }
}

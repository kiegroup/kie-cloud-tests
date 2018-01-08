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
package org.kie.cloud.integrationtests.drools;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireRulesSmokeIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchKieServerScenario> {

    private static final Logger logger = LoggerFactory.getLogger(DroolsSessionFailoverIntegrationTest.class);

    private KieServerControllerClient kieServerControllerClient;
    private KieServicesClient kieServerClient;
    private RuleServicesClient kieServerRuleServiceClient;

    private static KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "defaultKieSession";
    private static final String HELLO_RULE = "Hello.";
    private static final String WORLD_RULE = "World.";

    @Override
    protected WorkbenchKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
    }

    @Before
    public void setUp() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/hello-rules-snapshot").getFile());

        kieServerControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());

        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        kieServerRuleServiceClient = kieServerClient.getServicesClient(RuleServicesClient.class);
    }

    @Test
    public void executeSimpleRuleFailoverTest() throws InterruptedException {
        logger.debug("Register Kie Container to Kie Server");
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieServerControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, HELLO_RULES_PROJECT_NAME, HELLO_RULES_PROJECT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
        WorkbenchUtils.waitForContainerRegistration(kieServerControllerClient, serverInfo.getServerId(), CONTAINER_ID);

        logger.debug("Set Batch command");
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecutionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);
        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        logger.debug("Fire all rules");
        ServiceResponse<ExecutionResults> response = kieServerRuleServiceClient.executeCommandsWithResults(CONTAINER_ID, batchExecutionCommand);

        logger.debug("Check result of the drools command");
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();
        assertThat(result).isNotNull();
        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertThat(outcome).hasSize(2);

        assertThat(outcome.get(0)).startsWith(HELLO_RULE);
        assertThat(outcome.get(1)).startsWith(WORLD_RULE);
    }

}

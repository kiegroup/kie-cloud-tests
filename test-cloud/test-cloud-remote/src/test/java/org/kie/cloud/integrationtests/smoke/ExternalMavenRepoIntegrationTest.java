/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
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
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabaseScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;

@RunWith(Parameterized.class)
public class ExternalMavenRepoIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchKieServerScenario> {

    @Parameter
    public WorkbenchKieServerScenario workbenchKieServerScenario;

    @Parameters
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchKieServerScenario workbenchKieServerScenario = deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
        WorkbenchKieServerDatabaseScenario workbenchKieServerDatabaseScenario = deploymentScenarioFactory.getWorkbenchKieServerDatabaseScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();

        return Arrays.asList(new Object[][]{
            {workbenchKieServerScenario}, {workbenchKieServerDatabaseScenario}
        });
    }

    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "defaultKieSession";
    private static final String HELLO_RULE = "Hello.";
    private static final String WORLD_RULE = "World.";

    private static final String RULE_CONTAINER_ID = "rule-container-id";
    private static KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    @Override
    protected WorkbenchKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    @Before
    public void deployMavenProject() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/hello-rules-snapshot").getFile());
    }

    @Test
    public void testProcessFromExternalMavenRepo() {
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        kieServerClient.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, new ReleaseId(PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION)));
        kieServerClient.createContainer(RULE_CONTAINER_ID, new KieContainerResource(RULE_CONTAINER_ID, new ReleaseId(PROJECT_GROUP_ID, HELLO_RULES_PROJECT_NAME, HELLO_RULES_PROJECT_VERSION)));

        RuleServicesClient ruleClient = KieServerClientProvider.getRuleClient(deploymentScenario.getKieServerDeployment());

        List<Command<?>> commands = new ArrayList<>();
        BatchExecutionCommand batchExecutionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);
        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(RULE_CONTAINER_ID, batchExecutionCommand);

        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();
        assertThat(result).isNotNull();
        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertThat(outcome).hasSize(2);
        assertThat(outcome.get(0)).startsWith(HELLO_RULE);
        assertThat(outcome.get(1)).startsWith(WORLD_RULE);

        try {
            startAndCompleteUsertaskProcess();
        } catch (KieServicesException ex) {
            // OK for drools test.
            assertThat(ex.getMessage()).contains("Server that this client is connected to has no capabilities to handle ProcessServicesClient");
        }

    }

    private void startAndCompleteUsertaskProcess() {
        ProcessServicesClient processClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        UserTaskServicesClient taskClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployment());

        Long userTaskPid = processClient.startProcess(CONTAINER_ID, USERTASK_PROCESS_ID);
        Assertions.assertThat(userTaskPid).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasks(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).isNotNull().hasSize(1);

        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);

        ProcessInstance userTaskPi = processClient.getProcessInstance(CONTAINER_ID, userTaskPid);
        Assertions.assertThat(userTaskPi).isNotNull();
        Assertions.assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }
}

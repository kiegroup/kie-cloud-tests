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

package org.kie.cloud.integrationtests.testproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.KjarDeployer;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.integrationtests.shared.KieServerAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class FireRulesTestProvider {

    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "defaultKieSession";
    private static final String HELLO_RULE = "Hello.";
    private static final String WORLD_RULE = "World.";

    private static KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private FireRulesTestProvider() {}

    /**
     * Create provider instance
     *
     * @return provider instance
     */
    public static FireRulesTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     *
     * @param environment if not null, initialize this provider with the environment
     *
     * @return provider instance
     */
    public static FireRulesTestProvider create(DeploymentScenario<?> deploymentScenario) {
        FireRulesTestProvider provider = new FireRulesTestProvider();
        if (Objects.nonNull(deploymentScenario)) {
            provider.init(deploymentScenario);
        }
        return provider;
    }

    private void init(DeploymentScenario<?> deploymentScenario) {
        KjarDeployer.create(Kjar.HELLO_RULES_SNAPSHOT).deploy(deploymentScenario.getMavenRepositoryDeployment());
    }

    public void testDeployFromKieServerAndFireRules(KieServerDeployment kieServerDeployment) {
        String containerId = "testFireRules";
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);

        ServiceResponse<KieContainerResource> createContainer = kieServerClient.createContainer(containerId, new KieContainerResource(containerId, new ReleaseId(Kjar.HELLO_RULES_SNAPSHOT.getGroupId(),
                                                                                                                                                                 Kjar.HELLO_RULES_SNAPSHOT.getArtifactName(),
                                                                                                                                                                 Kjar.HELLO_RULES_SNAPSHOT.getVersion())));
        KieServerAssert.assertSuccess(createContainer);
        kieServerDeployment.waitForContainerRespin();

        try {
            testFireRules(kieServerDeployment, containerId);
        } finally {
            kieServerClient.disposeContainer(containerId);
            kieServerDeployment.waitForContainerRespin();
        }
    }

    public void testDeployFromWorkbenchAndFireRules(WorkbenchDeployment workbenchDeployment, KieServerDeployment kieServerDeployment, String gitRepositoryUrl) {
        String containerId = "testDeployFromWorkbenchAndFireRules";
        String containerAlias = "alias-testDeployFromWorkbenchAndFireRules";
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(workbenchDeployment);
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();

        try {
            WorkbenchUtils.deployProjectToWorkbench(gitRepositoryUrl, workbenchDeployment, Kjar.HELLO_RULES.getArtifactName());

            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), containerId, containerAlias, Kjar.HELLO_RULES, KieContainerStatus.STARTED);

            KieServerClientProvider.waitForContainerStart(kieServerDeployment, containerId);
            kieServerDeployment.waitForContainerRespin();

            testFireRules(kieServerDeployment, containerId);
        } finally {
            kieControllerClient.deleteContainerSpec(serverInfo.getServerId(), containerId);
            kieServerDeployment.waitForContainerRespin();
        }
    }

    public void testFireRules(KieServerDeployment kieServerDeployment, String containerId) {
        RuleServicesClient ruleClient = KieServerClientProvider.getRuleClient(kieServerDeployment);

        List<Command<?>> commands = new ArrayList<>();
        BatchExecutionCommand batchExecutionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);
        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(containerId, batchExecutionCommand);

        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();
        assertThat(result).isNotNull();
        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertThat(outcome).hasSize(2);
        assertThat(outcome.get(0)).startsWith(HELLO_RULE);
        assertThat(outcome.get(1)).startsWith(WORLD_RULE);
    }
}

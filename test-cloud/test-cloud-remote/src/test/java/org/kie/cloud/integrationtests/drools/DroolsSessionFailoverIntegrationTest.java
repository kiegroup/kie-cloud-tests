/*
 * Copyright 2017 JBoss by Red Hat.
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsSessionFailoverIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchRuntimeSmartRouterKieServerDatabaseScenario> {

    private static final Logger logger = LoggerFactory.getLogger(DroolsSessionFailoverIntegrationTest.class);
    private static final String SMART_ROUTER_ID = "test-kie-router";

    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "defaultKieSession";

    private KieServerMgmtControllerClient kieServerMgmtControllerClient;
    private KieServicesClient smartRouterServicesClient;
    private RuleServicesClient smartRouterRuleServiceClient;

    private KieServicesClient kieServerClient;

    private static KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private static final String FAST_RULE = "Fast rule executed";
    private static final String SLOW_RULE = "Slow rule executed";

    @Override
    protected WorkbenchRuntimeSmartRouterKieServerDatabaseScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withSmartRouterId(SMART_ROUTER_ID)
                .build();
    }

    @Before
    public void setUp() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/rule-project").getFile());

        kieServerMgmtControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());
        smartRouterServicesClient = KieServerClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerDeployment().getUsername(), deploymentScenario.getKieServerDeployment().getPassword(), TimeUnit.MINUTES.toMillis(10));
        smartRouterRuleServiceClient = smartRouterServicesClient.getServicesClient(RuleServicesClient.class);

        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        deploymentScenario.getSmartRouterDeployment().setRouterTimeout(Duration.ofMinutes(5));
    }

    @Test
    @Ignore("Activate when RHBPMS-5044 is done.")
    public void executeSimpleRuleFailoverTest() throws InterruptedException {
        logger.debug("Register Kie Container to Kie Server");
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieServerMgmtControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, RULE_PROJECT_NAME, RULE_PROJECT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
        WorkbenchUtils.waitForContainerRegistration(kieServerMgmtControllerClient, SMART_ROUTER_ID, CONTAINER_ID);

        logger.debug("Get Kie Server Instance");
        Instance kieServerInstance = deploymentScenario.getKieServerDeployment().getInstances().iterator().next();

        logger.debug("Set Batch command");
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand batchExecutionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);
        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        logger.debug("Start new thread for Kie server fail");
        Thread failoverThread = kieServerFailoverThread(kieServerInstance);
        failoverThread.start();

        logger.debug("Fire all rules");
        ServiceResponse<ExecutionResults> response;
        try {
            response = smartRouterRuleServiceClient.executeCommandsWithResults(CONTAINER_ID, batchExecutionCommand);
        } catch (Exception e) {
            failoverThread.join();
            throw e;
        }

        logger.debug("Check result of the drools command");
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();
        assertThat(result).isNotNull();
        List<String> outcome = (List<String>) result.getValue(LIST_OUTPUT_NAME);
        assertThat(outcome).hasSize(2);

        assertThat(outcome.get(0)).startsWith(FAST_RULE);
        assertThat(outcome.get(1)).startsWith(SLOW_RULE);

        logger.debug("Check Kie server deployment");
        List<Instance> kieServerInstances = deploymentScenario.getKieServerDeployment().getInstances();
        assertThat(kieServerInstances).hasSize(2).doesNotContain(kieServerInstance);
        logger.debug("Check Kie server logs. Only one Kie server executed command.");
        assertThat(instanceLogContains(kieServerInstances.get(0), FAST_RULE, SLOW_RULE)).isNotEqualTo(instanceLogContains(kieServerInstances.get(1), FAST_RULE, SLOW_RULE));
    }

    private boolean instanceLogContains(Instance instance, String... values) {
        for (String value : values) {
            if (!instance.getLogs().contains(value)) {
                return false;
            }
        }
        return true;
    }

    private Thread kieServerFailoverThread(Instance kieServerInstance) {
        return new Thread(() -> {
            logger.debug("Scale Kie server to 2");
            deploymentScenario.getKieServerDeployment().scale(2);
            logger.debug("Wait for scale");
            deploymentScenario.getKieServerDeployment().waitForScale();

            logger.debug("Force delete (kill) Kie server instance.");
            deploymentScenario.getKieServerDeployment().deleteInstances(kieServerInstance);
            logger.debug("Wait for scale");
            deploymentScenario.getKieServerDeployment().waitForScale();
        });
    }
}

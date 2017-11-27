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

package org.kie.cloud.integrationtests.architecture.emptymanagedkieserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.SmartRouterAdminClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.common.client.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;

public class EmptyManagedKieServerConnectionIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchRuntimeSmartRouterKieServerDatabaseScenario> {

    private static final String SMART_ROUTER_ID = "test-kie-router";

    private KieServerMgmtControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;
    private KieServicesClient smartRouterClient;
    private KieServerRouterClient smartRouterAdminClient;

    @Override
    protected WorkbenchRuntimeSmartRouterKieServerDatabaseScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withSmartRouterId(SMART_ROUTER_ID)
                .build();
    }

    @Before
    public void setUp() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());

        kieControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(deploymentScenario.getWorkbenchRuntimeDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        smartRouterClient = KieServerClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployment(), deploymentScenario.getKieServerDeployment().getUsername(), deploymentScenario.getKieServerDeployment().getPassword());
        smartRouterAdminClient = SmartRouterAdminClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployment());
    }

    @Test
    public void testConnectionBetweenDeployables() {
        String kieServerId = getKieServerId(kieServerClient);

        verifyControllerContainsServerTemplate(kieServerId);
        verifyControllerContainsServerTemplate(SMART_ROUTER_ID);
        verifyServerTemplateContainsKieServer(kieServerId);
        verifyServerTemplateContainsKieServer(SMART_ROUTER_ID);

        // Deploy container
        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
        WorkbenchUtils.waitForContainerRegistration(kieControllerClient, SMART_ROUTER_ID, CONTAINER_ID);

        verifyContainerIsDeployed(kieServerClient, CONTAINER_ID);
        verifyServerTemplateContainsContainer(kieServerId, CONTAINER_ID);
        verifyServerTemplateContainsContainer(SMART_ROUTER_ID, CONTAINER_ID);
        verifySmartRouterContainsKieServer(kieServerId);

        startAndCompleteProcess(kieServerClient);
        startAndCompleteProcess(smartRouterClient);
    }

    private String getKieServerId(KieServicesClient kieServerClient) {
        ServiceResponse<KieServerInfo> serverInfo = kieServerClient.getServerInfo();
        assertThat(serverInfo.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        return serverInfo.getResult().getServerId();
    }

    private void verifyContainerIsDeployed(KieServicesClient kieServerClient, String containerId) {
        ServiceResponse<KieContainerResourceList> containers = kieServerClient.listContainers();
        assertThat(containers.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        assertThat(containers.getResult().getContainers()).hasSize(1);
        assertThat(containers.getResult().getContainers().get(0).getContainerId()).isEqualTo(containerId);
        assertThat(containers.getResult().getContainers().get(0).getStatus()).isEqualTo(KieContainerStatus.STARTED);
    }

    private void verifyControllerContainsServerTemplate(String serverTemplate) {
        List<String> serverTemplateIds = kieControllerClient.listServerTemplates()
                                                            .stream()
                                                            .map(ServerTemplate::getId)
                                                            .collect(Collectors.toList());
        assertThat(serverTemplateIds).as("Server template " + serverTemplate + " isn't registered in controller.").contains(serverTemplate);
    }

    private void verifyServerTemplateContainsKieServer(String serverTemplate) {
        Collection<ServerInstanceKey> kieServers = kieControllerClient.getServerTemplate(serverTemplate).getServerInstanceKeys();
        assertThat(kieServers).hasSize(1);
    }

    private void verifyServerTemplateContainsContainer(String serverTemplate, String containerId) {
        Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
        assertThat(containersSpec).hasSize(1);
        assertThat(containersSpec.iterator().next().getId()).isEqualTo(containerId);
    }

    private void verifySmartRouterContainsKieServer(String kieServerId) {
        Configuration routerConfig = smartRouterAdminClient.getRouterConfig();

        assertThat(routerConfig.getHostsPerServer()).containsKey(kieServerId);
        assertThat(routerConfig.getHostsPerServer().get(kieServerId)).hasSize(1);
        assertThat(routerConfig.getHostsPerContainer()).containsKey(CONTAINER_ID);
        assertThat(routerConfig.getHostsPerContainer().get(CONTAINER_ID)).hasSize(1);
        assertThat(routerConfig.getContainerInfosPerContainer()).containsKey(CONTAINER_ID);
    }

    private void startAndCompleteProcess(KieServicesClient client) {
        ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);
        UserTaskServicesClient taskClient = client.getServicesClient(UserTaskServicesClient.class);

        Long userTaskPid = processClient.startProcess(CONTAINER_ID, USERTASK_PROCESS_ID);
        assertThat(userTaskPid).isNotNull();

        verifyProcessInstanceState(processClient, userTaskPid, org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);

        // TODO: Kie server user is hardcoded (see WorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilderImpl) due to scenario workaround,
        // therefore user defined in the process doesn't exist. Skipping task part of the test.
//        List<TaskSummary> tasks = taskClient.findTasks(USER_YODA, 0, 10);
//        assertThat(tasks).isNotNull().hasSize(1);
//
//        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);
//
//        verifyProcessInstanceState(processClient, userTaskPid, org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);

        processClient.abortProcessInstance(CONTAINER_ID, userTaskPid);

        verifyProcessInstanceState(processClient, userTaskPid, org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED);
    }

    private void verifyProcessInstanceState(ProcessServicesClient processClient, Long userTaskPid, int processInstanceState) {
        ProcessInstance userTaskPi = processClient.getProcessInstance(CONTAINER_ID, userTaskPid);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(processInstanceState);
    }
}

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

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.KjarDeployer;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.util.AwaitilityUtils;
import org.kie.cloud.tests.common.client.util.KieServerUtils;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.cloud.tests.common.time.Constants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;

import static org.assertj.core.api.Assertions.assertThat;

public class SmartRouterTestProvider {

    private static final int RETRIES_NUMBER = 5;
    private static final int PROCESS_NUMBER = 100;
    private static final String LOG_MESSAGE = "Log process was started";

    private SmartRouterTestProvider() {}

    /**
     * Create provider instance
     * 
     * @return provider instance
     */
    public static SmartRouterTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     * 
     * @param environment if not null, initialize this provider with the environment
     * 
     * @return provider instance
     */
    public static SmartRouterTestProvider create(DeploymentScenario<?> deploymentScenario) {
        SmartRouterTestProvider provider = new SmartRouterTestProvider();
        if (Objects.nonNull(deploymentScenario)) {
            provider.init(deploymentScenario);
        }
        return provider;
    }

    private void init(DeploymentScenario<?> deploymentScenario) {
        KjarDeployer.create(Kjar.DEFINITION_SNAPSHOT).deploy(deploymentScenario.getMavenRepositoryDeployment());
        KjarDeployer.create(Kjar.DEFINITION_101_SNAPSHOT).deploy(deploymentScenario.getMavenRepositoryDeployment());
    }

    public void testRouterLoadBalancing(WorkbenchDeployment workbenchDeployment,
                                        SmartRouterDeployment smartRouterDeployment,
                                        KieServerDeployment kieServerDeploymentOne,
                                        KieServerDeployment kieServerDeploymentTwo) {
        String containerId = "testRouterLoadBalancing";
        String containerAlias = "testRouterLoadBalancingAlias";

        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(workbenchDeployment);
        KieServicesClient smartRouterClient = KieServerClientProvider.getSmartRouterClient(smartRouterDeployment,
                                                                                           kieServerDeploymentOne.getUsername(), kieServerDeploymentOne.getPassword());
        KieServicesClient kieServerClientOne = KieServerClientProvider.getKieServerClient(kieServerDeploymentOne);
        KieServicesClient kieServerClientTwo = KieServerClientProvider.getKieServerClient(kieServerDeploymentTwo);

        try {
            deployContainerToServerTemplate(kieServerDeploymentOne, kieServerClientOne.getServerInfo().getResult(), kieControllerClient, containerId, containerAlias);
            deployContainerToServerTemplate(kieServerDeploymentTwo, kieServerClientTwo.getServerInfo().getResult(), kieControllerClient, containerId, containerAlias);

            ServiceResponse<KieServerInfo> kieServerInfo = smartRouterClient.getServerInfo();
            List<String> capabilities = kieServerInfo.getResult().getCapabilities();
            Assertions.assertThat(capabilities).isNotEmpty();

            QueryServicesClient queryServicesClient = smartRouterClient.getServicesClient(QueryServicesClient.class);
            List<ProcessDefinition> processDefinitions = queryServicesClient.findProcesses(0, 100);
            assertThat(processDefinitions).isNotNull();
            assertThat(processDefinitions.stream().anyMatch(p -> p.getId().equals(Constants.ProcessId.LOG)));

            ProcessServicesClient processServicesClient = smartRouterClient.getServicesClient(ProcessServicesClient.class);
            for (int i = 0; i < PROCESS_NUMBER; i++) {
                AwaitilityUtils.untilIsNotNull(() -> processServicesClient.startProcess(containerId, Constants.ProcessId.LOG));
            }

            assertLogMessages(kieServerDeploymentOne);
            assertLogMessages(kieServerDeploymentTwo);
        } finally {
            KieServerUtils.waitForContainerRespinAfter(kieServerDeploymentOne, () -> kieControllerClient.deleteContainerSpec(kieServerClientOne.getServerInfo().getResult().getServerId(), containerId));
            KieServerUtils.waitForContainerRespinAfter(kieServerDeploymentTwo, () -> kieControllerClient.deleteContainerSpec(kieServerClientTwo.getServerInfo().getResult().getServerId(), containerId));
        }
    }

    private void deployContainerToServerTemplate(KieServerDeployment deployment,
                                                 KieServerInfo serverInfo,
                                                 KieServerControllerClient kieControllerClient,
                                                 String containerId,
                                                 String containerAlias) {
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(),
                                         containerId, containerAlias, Kjar.DEFINITION_SNAPSHOT, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deployment, containerId);
        deployment.waitForContainerRespin();
    }

    private void assertLogMessages(KieServerDeployment kieServerDeployment) {
        for (Instance kieServerInstance : kieServerDeployment.getInstances()) {
            Assertions.assertThat(StringUtils.countMatches(kieServerInstance.getLogs(), LOG_MESSAGE))
                    .isGreaterThan(PROCESS_NUMBER / 4);
        }
    }

    public void testRouterContainerIdLoadBalancing(SmartRouterDeployment smartRouterDeployment,
                                                   KieServerDeployment kieServerDeploymentOne,
                                                   KieServerDeployment kieServerDeploymentTwo) {
        String containerId = "testRouterContainerIdLoadBalancing";
        String containerAlias = "testRouterContainerIdLoadBalancingAlias";
        String containerIdUpdated = "testRouterContainerIdLoadBalancingUpdated";

        KieServicesClient smartRouterClient = KieServerClientProvider.getSmartRouterClient(smartRouterDeployment,
                                                                                           kieServerDeploymentOne.getUsername(), kieServerDeploymentOne.getPassword());
        KieServicesClient kieServerClientOne = KieServerClientProvider.getKieServerClient(kieServerDeploymentOne);
        KieServicesClient kieServerClientTwo = KieServerClientProvider.getKieServerClient(kieServerDeploymentTwo);

        try {
            deployProject(kieServerDeploymentOne, kieServerClientOne, containerId, containerAlias, Kjar.DEFINITION_SNAPSHOT);
            deployProject(kieServerDeploymentTwo, kieServerClientTwo, containerId, containerAlias, Kjar.DEFINITION_SNAPSHOT);

            verifyProcessAvailableInContainer(smartRouterClient, containerId, Constants.ProcessId.USERTASK);

            deployProject(kieServerDeploymentOne, kieServerClientOne, containerIdUpdated, containerAlias, Kjar.DEFINITION_101_SNAPSHOT);

            verifyProcessAvailableInContainer(smartRouterClient, containerIdUpdated, Constants.ProcessId.UPDATED_USERTASK);
        } finally {
            KieServerUtils.waitForContainerRespinAfterDisposeContainer(kieServerDeploymentOne, containerId);
            KieServerUtils.waitForContainerRespinAfterDisposeContainer(kieServerDeploymentOne, containerIdUpdated);
            KieServerUtils.waitForContainerRespinAfterDisposeContainer(kieServerDeploymentTwo, containerId);
        }
    }

    public void testRouterContainerAliasLoadBalancing(SmartRouterDeployment smartRouterDeployment,
                                                      KieServerDeployment kieServerDeploymentOne,
                                                      KieServerDeployment kieServerDeploymentTwo) {
        String containerId = "testRouterContainerAliasLoadBalancing";
        String containerAlias = "testRouterContainerAliasLoadBalancingAlias";

        KieServicesClient smartRouterClient = KieServerClientProvider.getSmartRouterClient(smartRouterDeployment,
                                                                                           kieServerDeploymentOne.getUsername(), kieServerDeploymentOne.getPassword());
        KieServicesClient kieServerClientOne = KieServerClientProvider.getKieServerClient(kieServerDeploymentOne);
        KieServicesClient kieServerClientTwo = KieServerClientProvider.getKieServerClient(kieServerDeploymentTwo);

        try {
            deployProject(kieServerDeploymentOne, kieServerClientOne, containerId, containerAlias, Kjar.DEFINITION_SNAPSHOT);
            deployProject(kieServerDeploymentTwo, kieServerClientTwo, containerId, containerAlias, Kjar.DEFINITION_SNAPSHOT);

            for (int i = 0; i < RETRIES_NUMBER; i++) {
                verifyProcessAvailableInContainer(smartRouterClient, containerAlias, Constants.ProcessId.LOG);
            }

            assertThat(kieServerDeploymentOne.getInstances()).hasSize(1);
            assertThat(kieServerDeploymentOne.getInstances().get(0).getLogs()).contains(LOG_MESSAGE);
            assertThat(kieServerDeploymentTwo.getInstances()).hasSize(1);
            assertThat(kieServerDeploymentTwo.getInstances().get(0).getLogs()).contains(LOG_MESSAGE);
        } finally {
            KieServerUtils.waitForContainerRespinAfterDisposeContainer(kieServerDeploymentOne, containerId);
            KieServerUtils.waitForContainerRespinAfterDisposeContainer(kieServerDeploymentTwo, containerId);
        }
    }

    private void deployProject(KieServerDeployment kieServerDeployment, KieServicesClient kieServerClient, String containerId, String containerAlias, Kjar project) {
        KieContainerResource resource = new KieContainerResource(containerId,
                                                                 new ReleaseId(project.getGroupId(), project.getArtifactName(), project.getVersion()));
        resource.setContainerAlias(containerAlias);

        KieServerUtils.waitForContainerRespinAfter(kieServerDeployment, () -> kieServerClient.createContainer(containerId, resource));
    }

    private void verifyProcessAvailableInContainer(KieServicesClient kieServerClient, String containerId, String processId) {
        ProcessServicesClient processClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        AwaitilityUtils.untilIsNotNull(() -> processClient.startProcess(containerId, processId));
    }
}

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

package org.kie.cloud.integrationtests.scaling;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterKieServerDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;

public class SmartRouterLoadBalancing extends
        AbstractCloudIntegrationTest<WorkbenchRuntimeSmartRouterKieServerDatabaseScenario> {

    private KieServerMgmtControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;
    private KieServicesClient kieServerClientRouter;

    private static final int PROCESS_NUMBER = 100;
    private static final String LOG_MESSAGE = "Log process was started";

    @Override protected WorkbenchRuntimeSmartRouterKieServerDatabaseScenario createDeploymentScenario(
            DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory
                .getWorkbenchRuntimeSmartRouterKieServerDatabaseScenarioBuilder()
                .withExternalMavenRepo(
                        MavenConstants.getMavenRepoUrl(),
                        MavenConstants.getMavenRepoUser(),
                        MavenConstants.getMavenRepoPassword())
                .build();
    }

    @Test
    public void testRouterLoadBalancing() {
        MavenDeployer.buildAndDeployMavenProject(
                ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());

        kieControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(
                deploymentScenario.getWorkbenchRuntimeDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(),
                CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME,
                DEFINITION_PROJECT_SNAPSHOT_VERSION, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        deploymentScenario.getKieServerDeployment().scale(2);

        kieServerClientRouter = KieServerClientProvider.getSmartRouterClient(
                deploymentScenario.getSmartRouterDeployment(),
                deploymentScenario.getKieServerDeployment().getUsername(),
                deploymentScenario.getKieServerDeployment().getPassword());

        ServiceResponse<KieServerInfo> kieServerInfo = kieServerClientRouter.getServerInfo();
        List<String> capabilities = kieServerInfo.getResult().getCapabilities();
        Assertions.assertThat(capabilities).isNotEmpty();

        QueryServicesClient queryServicesClient = kieServerClientRouter.getServicesClient(QueryServicesClient.class);
        List<ProcessDefinition> processDefinitions = queryServicesClient.findProcesses(0, 100);
        Assertions.assertThat(processDefinitions).isNotNull();
        Assertions.assertThat(processDefinitions.stream().anyMatch(p -> p.getId().equals(LOG_PROCESS_ID)));

        ProcessServicesClient processServicesClient = kieServerClientRouter.getServicesClient(
                ProcessServicesClient.class);
        for (int i = 0; i < PROCESS_NUMBER; i++) {
            processServicesClient.startProcess(CONTAINER_ID, LOG_PROCESS_ID);
        }

        List<Instance> kieServerInstances = deploymentScenario.getKieServerDeployment().getInstances();
        for (Instance kieServerInstance : kieServerInstances) {
            Assertions.assertThat(StringUtils.countMatches(kieServerInstance.getLogs(), LOG_MESSAGE))
                    .isGreaterThan(PROCESS_NUMBER / 4);
        }
    }
}

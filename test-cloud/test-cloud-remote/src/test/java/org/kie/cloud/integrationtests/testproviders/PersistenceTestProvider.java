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

import java.util.Objects;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.util.AwaitilityUtils;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceTestProvider {

    private PersistenceTestProvider() {}

    /**
     * Create provider instance
     *
     * @return provider instance
     */
    public static PersistenceTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     *
     * @param environment if not null, initialize this provider with the environment
     *
     * @return provider instance
     */
    public static PersistenceTestProvider create(DeploymentScenario<?> deploymentScenario) {
        PersistenceTestProvider provider = new PersistenceTestProvider();
        if (Objects.nonNull(deploymentScenario)) {
            provider.init(deploymentScenario);
        }
        return provider;
    }

    private void init(DeploymentScenario<?> deploymentScenario) {}

    public void testControllerPersistence(WorkbenchKieServerScenario deploymentScenario, Kjar container) {
        String containerId = "testControllerPersistence";

        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), containerId, containerId, container, KieContainerStatus.STARTED);
        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), containerId);

        try {
            verifyOneServerTemplateWithContainer(kieControllerClient, serverInfo.getServerId(), containerId);

            scaleToZeroAndBackToOne(deploymentScenario.getWorkbenchDeployment());

            verifyOneServerTemplateWithContainer(kieControllerClient, serverInfo.getServerId(), containerId);
        } finally {
            kieControllerClient.deleteContainerSpec(serverInfo.getServerId(), containerId);
        }
    }

    private static void scaleToZeroAndBackToOne(Deployment deployment) {
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(1);
        deployment.waitForScale();
    }

    private static void verifyOneServerTemplateWithContainer(KieServerControllerClient kieControllerClient, String kieServerId, String containerId) {
        AwaitilityUtils.untilAsserted(() -> {
            ServerTemplateList serverTemplates = kieControllerClient.listServerTemplates();
            assertThat(serverTemplates.getServerTemplates()).as("Number of server templates differ.").hasSize(1);

            ServerTemplate serverTemplate = serverTemplates.getServerTemplates()[0];
            assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
            assertThat(serverTemplate.getId()).isEqualTo(kieServerId);
            assertThat(serverTemplate.getContainersSpec()).anyMatch(containerSpec -> containerSpec.getId().equals(containerId));
        });
    }
}

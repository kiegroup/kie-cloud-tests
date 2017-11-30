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
package org.kie.cloud.integrationtests.architecture;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.management.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;

public abstract class AbstractCloudArchitectureIntegrationTest extends AbstractCloudIntegrationTest<GenericScenario> {

    protected static final String SMART_ROUTER_ID = "test-kie-router";

    protected static final String
            CONTAINER_ID_ABC = CONTAINER_ID + "-abc",
            CONTAINER_ID_DEF = CONTAINER_ID + "-def",
            CONTAINER_ID_GHI = CONTAINER_ID + "-ghi";
    protected static final String
            KIE_CONTAINER_DEPLOYMENT_ABC = CONTAINER_ID_ABC + "=" + PROJECT_GROUP_ID + ":" + DEFINITION_PROJECT_NAME + ":" + DEFINITION_PROJECT_VERSION,
            KIE_CONTAINER_DEPLOYMENT_DEF = CONTAINER_ID_DEF + "=" + PROJECT_GROUP_ID + ":" + DEFINITION_PROJECT_NAME + ":" + DEFINITION_PROJECT_VERSION,
            KIE_CONTAINER_DEPLOYMENT_GHI = CONTAINER_ID_GHI + "=" + PROJECT_GROUP_ID + ":" + DEFINITION_PROJECT_NAME + ":" + DEFINITION_PROJECT_VERSION;

    protected static final String REPO_BRANCH = "master";

    protected static final String CONTROLLER_NAME = "controller";
    protected static final String SMART_ROUTER_NAME = "smart-router";

    protected static final String
            KIE_SERVER_ABC_NAME = "kie-server-abc",
            KIE_SERVER_DEF_NAME = "kie-server-def",
            KIE_SERVER_GHI_NAME = "kie-server-ghi";

    protected static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    protected static final String PORT = "80";

    protected String repositoryName;

    protected String getKieServerId(KieServicesClient kieServerClient) {
        ServiceResponse<KieServerInfo> serverInfo = kieServerClient.getServerInfo();
        assertThat(serverInfo.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        return serverInfo.getResult().getServerId();
    }

    protected void assertClientHasConnectedKieServerCount(KieServicesClient kieServerClient, final int numberOfExpectedServers) {
        Set<String> locations = new HashSet<>();
        // Repeat this request several time to be sure we have all locations.
        for (int i = 0; i < numberOfExpectedServers * 3; i++) {
            String location = getKieServerLocation(kieServerClient);
            if (!location.isEmpty()) {
                locations.add(location);
            }
        }
        assertThat(locations).hasSize(numberOfExpectedServers);
    }

    // KieServerClient can be affected by OpenShift router or Kie Server load ballancer.
    private String getKieServerLocation(KieServicesClient kieServerClient) {
        // getServer info request can be handled by one of N Kie server instances
        ServiceResponse<KieServerInfo> serverInfo = kieServerClient.getServerInfo();
        if (serverInfo.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
            return serverInfo.getResult().getLocation();
        }
        return new String();
    }

    protected void assertClientHasNotConnectedKieServer(KieServicesClient kieServerClient) {
        ServiceResponse<KieServerInfo> serverInfo;
        try {
            serverInfo = kieServerClient.getServerInfo();
        } catch (KieServicesHttpException ex) {
            //ok
            assertThat(ex.getHttpCode()).isEqualTo(503);
            return;
        }
        assertThat(serverInfo.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
        assertThat(serverInfo.getMsg()).contains("404");
    }

    protected void verifyContainerIsDeployed(KieServicesClient kieServerClient, String containerId) {
        ServiceResponse<KieContainerResourceList> containers = kieServerClient.listContainers();
        assertThat(containers.getType()).isEqualTo(ServiceResponse.ResponseType.SUCCESS);

        List<KieContainerResource> containerList = containers.getResult().getContainers();
        assertThat(containerList).hasSize(1);
        KieContainerResource container = containerList.get(0);
        assertThat(container.getContainerId()).isEqualTo(containerId);
        assertThat(container.getStatus()).isEqualTo(KieContainerStatus.STARTED);
    }

    protected void verifyServerTemplateContainsContainers(KieServerMgmtControllerClient kieControllerClient, String serverTemplate, String... containerIds) {
        Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
        assertThat(containersSpec).hasSize(containerIds.length);
        List<String> containersSpecIds = getAllContainersSpecIds(containersSpec);
        assertThat(containersSpecIds).containsExactlyInAnyOrder(containerIds);
    }

    private List<String> getAllContainersSpecIds(Collection<ContainerSpec> containerSpecs) {
        return containerSpecs.stream().map(spec -> spec.getId()).collect(Collectors.toList());
    }

    protected void verifyServerTemplateContainsKieServers(KieServerMgmtControllerClient kieControllerClient, String serverTemplate, int numberOfKieServers) {
        Collection<ServerInstanceKey> kieServers = kieControllerClient.getServerTemplate(serverTemplate).getServerInstanceKeys();
        assertThat(kieServers).hasSize(numberOfKieServers);
    }

    protected void verifySmartRouterContainsKieServers(KieServerRouterClient smartRouterAdminClient, int numberOfKieServers, List<String> containerIds, String... kieServerIds) {
        SoftAssertions softly = new SoftAssertions();
        Configuration routerConfig = smartRouterAdminClient.getRouterConfig();

        softly.assertThat(routerConfig.getHostsPerServer()).containsKeys(kieServerIds);

        for (String containerId : containerIds) {
            // TODO: Commented until "Duplicate Kie server registration" issue gets resolved
//            for (String kieServerId : kieServerIds) {
//                softly.assertThat(routerConfig.getHostsPerServer().get(kieServerId)).hasSize(numberOfKieServers);
//            }
            softly.assertThat(routerConfig.getHostsPerContainer()).containsKey(containerId);
            // TODO: Commented until "Duplicate Kie server registration" issue gets resolved
//        softly.assertThat(routerConfig.getHostsPerContainer().get(containerId)).hasSize(numberOfKieServers);
            softly.assertThat(routerConfig.getContainerInfosPerContainer()).containsKey(containerId);
        }
        softly.assertAll();
    }

    protected void verifySmartRouterHostPerServerContainsContainers(KieServerRouterClient smartRouterAdminClient, String kieServerId, String... containersIds) {
        Configuration routerConfig = smartRouterAdminClient.getRouterConfig();
        assertThat(routerConfig.getHostsPerServer().get(kieServerId)).contains(containersIds);
    }

    protected void verifySmartRouterHostPerContainerContainsServers(KieServerRouterClient smartRouterAdminClient, String containerId, String... kieServerIds) {
        Configuration routerConfig = smartRouterAdminClient.getRouterConfig();
        assertThat(routerConfig.getHostsPerContainer().get(containerId)).contains(kieServerIds);
    }

    protected static void scaleKieServerTo(int count, KieServerDeployment... deployments) {
        scaleKieServerTo(Arrays.asList(deployments), count);
    }

    protected static void scaleKieServerTo(List<KieServerDeployment> deployments, int count) {
        for (Deployment deployment : deployments) {
            deployment.scale(count);
            deployment.waitForScale();
        }
    }
}
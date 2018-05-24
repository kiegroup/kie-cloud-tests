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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;

public class SmartRouterUpdateRedirectionIntegrationTest extends AbstractCloudIntegrationTest<GenericScenario> {

    private static final int RETRIES_NUMBER = 5;

    private static final String CONTAINER_ID_UPDATED = "container-updated";
    private static final String LOG_MESSAGE = "Log process was started";

    private final String smartRouterName = "smart-router-" + UUID.randomUUID().toString().substring(0, 4);
    private final String smartRouterHostname = smartRouterName + DeploymentConstants.getDefaultDomainSuffix();
    private final String smartRouterPort = "80";

    private KieServerDeployment kieServerDeployment1;
    private KieServerDeployment kieServerDeployment2;

    private KieServicesClient kieServerClient1;
    private KieServicesClient kieServerClient2;
    private KieServicesClient smartRouterClient;

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        DeploymentSettings kieServer1 = deploymentScenarioFactory.getKieServerMySqlSettingsBuilder()
                .withMavenRepoUrl(MavenConstants.getMavenRepoUrl())
                .withMavenRepoUser(MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withKieServerSyncDeploy(true)
                .withApplicationName("kie-server-1")
                .withSmartRouterConnection(smartRouterHostname, smartRouterPort)
                .build();
        DeploymentSettings kieServer2 = deploymentScenarioFactory.getKieServerMySqlSettingsBuilder()
                .withMavenRepoUrl(MavenConstants.getMavenRepoUrl())
                .withMavenRepoUser(MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withKieServerSyncDeploy(true)
                .withApplicationName("kie-server-2")
                .withSmartRouterConnection(smartRouterHostname, smartRouterPort)
                .build();
        DeploymentSettings smartRouter = deploymentScenarioFactory.getSmartRouterSettingsBuilder()
                .withHostame(smartRouterHostname)
                .withSmartRouterExternalUrl("http://" + smartRouterHostname + ":" + smartRouterPort)
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServer1)
                .withKieServer(kieServer2)
                .withSmartRouter(smartRouter)
                .build();
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-101-snapshot").getFile());
    }

    @Before
    public void prepateClientsAndProject() {
        kieServerDeployment1 = deploymentScenario.getKieServerDeployments().get(0);
        kieServerDeployment2 = deploymentScenario.getKieServerDeployments().get(1);
        SmartRouterDeployment smartRouterDeployment = deploymentScenario.getSmartRouterDeployments().get(0);

        kieServerClient1 = KieServerClientProvider.getKieServerClient(kieServerDeployment1);
        kieServerClient2 = KieServerClientProvider.getKieServerClient(kieServerDeployment2);
        smartRouterClient = KieServerClientProvider.getSmartRouterClient(smartRouterDeployment,
                kieServerDeployment1.getUsername(), kieServerDeployment1.getPassword());
    }

    @Test
    public void testRouterContainerIdLoadBalancing() {
        deployProject(kieServerClient1, CONTAINER_ID, Kjar.DEFINITION_SNAPSHOT);
        deployProject(kieServerClient2, CONTAINER_ID, Kjar.DEFINITION_SNAPSHOT);

        verifyProcessAvailableInContainer(smartRouterClient, CONTAINER_ID, USERTASK_PROCESS_ID);

        deployProject(kieServerClient1, CONTAINER_ID_UPDATED, Kjar.DEFINITION_101_SNAPSHOT);

        for (int i = 0; i < RETRIES_NUMBER; i++) {
            verifyProcessAvailableInContainer(smartRouterClient, CONTAINER_ID_UPDATED, UPDATED_USERTASK_PROCESS_ID);
        }
    }

    @Test
    public void testRouterContainerAliasLoadBalancing() {
        deployProject(kieServerClient1, CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION_SNAPSHOT);
        deployProject(kieServerClient2, CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION_SNAPSHOT);

        for (int i = 0; i < RETRIES_NUMBER; i++) {
            verifyProcessAvailableInContainer(smartRouterClient, CONTAINER_ALIAS, LOG_PROCESS_ID);
        }

        assertThat(kieServerDeployment1.getInstances()).hasSize(1);
        assertThat(kieServerDeployment1.getInstances().get(0).getLogs()).contains(LOG_MESSAGE);
        assertThat(kieServerDeployment2.getInstances()).hasSize(1);
        assertThat(kieServerDeployment2.getInstances().get(0).getLogs()).contains(LOG_MESSAGE);
    }

    private void deployProject(KieServicesClient kieServerClient, String containerId, Kjar project) {
        deployProject(kieServerClient, containerId, CONTAINER_ALIAS, project);
    }

    private void deployProject(KieServicesClient kieServerClient, String containerId, String containerAlias, Kjar project) {
        KieContainerResource resource = new KieContainerResource(containerId,
                new ReleaseId(project.getGroupId(), project.getName(), project.getVersion()));
        resource.setContainerAlias(containerAlias);

        kieServerClient.createContainer(containerId, resource);
    }

    private void verifyProcessAvailableInContainer(KieServicesClient kieServerClient, String containerId, String processId) {
        ProcessServicesClient processClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        Long pId = processClient.startProcess(containerId, processId);
        assertThat(pId).isNotNull();
    }
}

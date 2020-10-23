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
package org.kie.cloud.integrationtests.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.common.util.AwaitilityUtils;
import org.kie.cloud.git.GitUtils;
import org.kie.cloud.integrationtests.category.OperatorNotSupported;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
@Category({OperatorNotSupported.class}) // Operator doesn't support scaling Workbench to 0 for this scenario
public class WorkbenchPersistenceIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {

    private static final String REPOSITORY_NAME = generateNameWithPrefix(WorkbenchPersistenceIntegrationTest.class.getSimpleName());
    private static final Logger logger = LoggerFactory.getLogger(WorkbenchPersistenceIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieDeploymentScenario<?> workbenchKieServerScenario;

    private WorkbenchClient workbenchClient;
    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    private WorkbenchDeployment workbenchDeployment;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        GitSettings gitSettings = GitSettings.fromProperties()
                                             .withRepository(REPOSITORY_NAME,
                                                             WorkbenchPersistenceIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        try {
            WorkbenchKieServerPersistentScenario workbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                    .withGitSettings(gitSettings)
                    .build();

            scenarios.add(new Object[]{"Workbench + KIE Server - Persistent", workbenchKieServerPersistentScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("Workbench + KIE Server - Persistent is skipped.", ex);
        }

        try {
            ClusteredWorkbenchKieServerDatabasePersistentScenario clusteredWorkbenchKieServerDatabasePersistentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                    .withGitSettings(gitSettings)
                    .build();
            scenarios.add(new Object[]{"Clustered Workbench + KIE Server + Database - Persistent", clusteredWorkbenchKieServerDatabasePersistentScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("Clustered Workbench + KIE Server + Database is skipped.", ex);
        }

        return scenarios;
    }

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    @Before
    public void setUp() {
        workbenchDeployment = deploymentScenario.getWorkbenchDeployments().get(0);
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(workbenchDeployment);
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(workbenchDeployment);
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
    }

    @After
    public void tearDown() {
        GitUtils.deleteGitRepository(REPOSITORY_NAME, deploymentScenario);
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        WorkbenchUtils.deployProjectToWorkbench(REPOSITORY_NAME, deploymentScenario, DEFINITION_PROJECT_NAME);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        verifyOneServerTemplateWithContainer(CONTAINER_ID);

        scaleToZeroAndToOne(workbenchDeployment);

        verifyOneServerTemplateWithContainer(CONTAINER_ID);
    }

    @Test
    public void testWorkbenchProjectPersistence() {
        workbenchClient.createSpace(SPACE_NAME, workbenchDeployment.getUsername());
        workbenchClient.createProject(SPACE_NAME, DEFINITION_PROJECT_NAME, PROJECT_GROUP_ID, DEFINITION_PROJECT_VERSION);

        assertSpaceAndProjectExists(SPACE_NAME, DEFINITION_PROJECT_NAME);

        scaleToZeroAndToOne(workbenchDeployment);

        assertSpaceAndProjectExists(SPACE_NAME, DEFINITION_PROJECT_NAME);
        workbenchClient.deployProject(SPACE_NAME, DEFINITION_PROJECT_NAME);

        scaleToZeroAndToOne(workbenchDeployment);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployments().get(0), CONTAINER_ID);

        AwaitilityUtils.untilAsserted(kieServerClient::listContainers, containersResponse -> {
            assertThat(containersResponse.getType()).isEqualTo(ResponseType.SUCCESS);
            assertThat(containersResponse.getResult().getContainers()).hasSize(1);
            assertThat(containersResponse.getResult().getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
        });
    }

    private void verifyOneServerTemplate() {
        ServerTemplate[] serverTemplates = AwaitilityUtils.untilIsNotEmpty(() -> kieControllerClient.listServerTemplates().getServerTemplates());
        assertThat(serverTemplates).as("Number of server templates differ.").hasSize(1);
    }

    private void verifyOneServerTemplateWithContainer(String containerId) {
        AwaitilityUtils.untilAsserted(() -> kieControllerClient.listServerTemplates().getServerTemplates()[0], serverTemplate -> {
            assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
            // Skip check on URL as the workbench has an internal IP to KIE server and we only have the route here
            // assertThat(serverTemplate.getServerInstanceKeys().iterator().next().getUrl()).isEqualTo(kieServerLocation);
            assertThat(serverTemplate.getContainersSpec()).hasSize(1);
            assertThat(serverTemplate.getContainersSpec().iterator().next().getId()).isEqualTo(containerId);
        });
    }

    private void assertSpaceAndProjectExists(String spaceName, String projectName) {
        Collection<Space> spaces = workbenchClient.getSpaces();
        assertThat(spaces.stream().anyMatch(n -> n.getName().equals(spaceName))).as("Space " + spaceName + " not found.").isTrue();

        Collection<ProjectResponse> projects = workbenchClient.getProjects(spaceName);
        assertThat(projects.stream().anyMatch(n -> n.getName().equals(projectName))).as("Project " + projectName + " not found.").isTrue();
    }

    private void scaleToZeroAndToOne(Deployment deployment) {
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(1);
        deployment.waitForScale();

        verifyOneServerTemplate();
    }
}
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

import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerDatabaseScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

@RunWith(Parameterized.class)
public class WorkbenchPersistenceIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchKieServerScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public WorkbenchKieServerScenario workbenchKieServerScenario;

    private WorkbenchClient workbenchClient;
    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchKieServerScenario workbenchKieServerScenario = deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder()
                .build();
        WorkbenchKieServerDatabasePersistentScenario workbenchKieServerDatabasePersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerDatabasePersistentScenarioBuilder()
                .build();

        return Arrays.asList(new Object[][]{
            {"Workbench + KIE Server", workbenchKieServerScenario},
            {"Workbench + KIE Server + Database - Persistent", workbenchKieServerDatabasePersistentScenario}
        });
    }

    @Override
    protected WorkbenchKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    @Before
    public void setUp() {
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        String repositoryName = gitProvider.createGitRepositoryWithPrefix(deploymentScenario.getWorkbenchDeployment().getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), deploymentScenario.getWorkbenchDeployment(), DEFINITION_PROJECT_NAME);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        String kieServerLocation = serverInfo.getLocation();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_NAME, DEFINITION_PROJECT_VERSION, KieContainerStatus.STARTED);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);

        scaleToZeroAndToOne(deploymentScenario.getWorkbenchDeployment());

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);
    }

    @Test
    public void testWorkbenchProjectPersistence() {
        workbenchClient.createSpace(SPACE_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
        workbenchClient.createProject(SPACE_NAME, DEFINITION_PROJECT_NAME, PROJECT_GROUP_ID, DEFINITION_PROJECT_VERSION);

        assertSpaceAndProjectExists(SPACE_NAME, DEFINITION_PROJECT_NAME);

        scaleToZeroAndToOne(deploymentScenario.getWorkbenchDeployment());

        assertSpaceAndProjectExists(SPACE_NAME, DEFINITION_PROJECT_NAME);
        workbenchClient.deployProject(SPACE_NAME, DEFINITION_PROJECT_NAME);

        scaleToZeroAndToOne(deploymentScenario.getWorkbenchDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, DEFINITION_PROJECT_NAME, DEFINITION_PROJECT_VERSION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        ServiceResponse<KieContainerResourceList> containersResponse = kieServerClient.listContainers();
        assertThat(containersResponse.getType()).isEqualTo(ResponseType.SUCCESS);
        assertThat(containersResponse.getResult().getContainers()).hasSize(1);
        assertThat(containersResponse.getResult().getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
    }

    private void verifyOneServerTemplateWithContainer(String kieServerLocation, String containerId) {
        ServerTemplateList serverTemplates = kieControllerClient.listServerTemplates();
        assertThat(serverTemplates.getServerTemplates()).as("Number of server templates differ.").hasSize(1);

        ServerTemplate serverTemplate = serverTemplates.getServerTemplates()[0];
        assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
        assertThat(serverTemplate.getServerInstanceKeys().iterator().next().getUrl()).isEqualTo(kieServerLocation);
        assertThat(serverTemplate.getContainersSpec()).hasSize(1);
        assertThat(serverTemplate.getContainersSpec().iterator().next().getId()).isEqualTo(containerId);
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
    }
}

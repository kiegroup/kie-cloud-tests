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
import java.util.Collection;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.cloud.provider.git.Git;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
@Ignore("Ignored as the tests are affected by RHPAM-1354. Unignore when the JIRA will be fixed.")
public class WorkbenchPersistenceIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieDeploymentScenario workbenchKieServerScenario;

    private String repositoryName;

    private WorkbenchClient workbenchClient;
    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    private WorkbenchDeployment workbenchDeployment;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchKieServerPersistentScenario workbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .build();

        ClusteredWorkbenchKieServerDatabasePersistentScenario clusteredWorkbenchKieServerDatabasePersistentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                .build();

        return Arrays.asList(new Object[][]{
            {"Workbench + KIE Server - Persistent", workbenchKieServerPersistentScenario},
            {"Clustered Workbench + KIE Server + Database - Persistent", clusteredWorkbenchKieServerDatabasePersistentScenario},
        });
    }

    @Override
    protected KieDeploymentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
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
        if (repositoryName != null) {
            Git.getProvider().deleteGitRepository(repositoryName);
            repositoryName = null;
        }
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        repositoryName = Git.getProvider().createGitRepositoryWithPrefix(workbenchDeployment.getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(Git.getProvider().getRepositoryUrl(repositoryName), workbenchDeployment, DEFINITION_PROJECT_NAME);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        String kieServerLocation = serverInfo.getLocation();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);

        scaleToZeroAndToOne(workbenchDeployment);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);
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
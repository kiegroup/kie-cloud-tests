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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.RepositoryResponse;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class WorkbenchPersistenceIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    private static final String REPOSITORY_NAME = "myRepo";

    private static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    private static final String PROJECT_NAME = "definition-project";
    private static final String PROJECT_VERSION = "1.0.0.Final";

    private static final String CONTAINER_ID = "cont-id";
    private static final String CONTAINER_ALIAS = "cont-alias";

    private WorkbenchClient workbenchClient;
    private KieServerMgmtControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    @Override
    protected WorkbenchWithKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
    }

    @Before
    public void setUp() {
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(deploymentScenario.getWorkbenchDeployment());
        kieControllerClient = KieServerControllerClientProvider.getKieServerMgmtControllerClient(deploymentScenario.getWorkbenchDeployment());
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        WorkbenchUtils.deployProjectToWorkbench(gitProvider, deploymentScenario.getWorkbenchDeployment(), PROJECT_NAME);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        String kieServerLocation = serverInfo.getLocation();
        kieControllerClient.saveContainerSpec(serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, PROJECT_NAME, PROJECT_VERSION, KieContainerStatus.STARTED);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);

        scaleToZeroAndToOne(deploymentScenario.getWorkbenchDeployment());

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);
    }

    @Test
    public void testWorkbenchProjectPersistence() {
        workbenchClient.createOrganizationalUnit(ORGANIZATION_UNIT_NAME, deploymentScenario.getWorkbenchDeployment().getUsername());
        workbenchClient.createRepository(ORGANIZATION_UNIT_NAME, REPOSITORY_NAME);
        workbenchClient.createProject(REPOSITORY_NAME, PROJECT_NAME, PROJECT_GROUP_ID, PROJECT_VERSION);

        assertRepositoryAndProjectExists(REPOSITORY_NAME, PROJECT_NAME);

        scaleToZeroAndToOne(deploymentScenario.getWorkbenchDeployment());

        assertRepositoryAndProjectExists(REPOSITORY_NAME, PROJECT_NAME);
        workbenchClient.deployProject(REPOSITORY_NAME, PROJECT_NAME);

        scaleToZeroAndToOne(deploymentScenario.getWorkbenchDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        kieControllerClient.saveContainerSpec(serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, PROJECT_NAME, PROJECT_VERSION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        ServiceResponse<KieContainerResourceList> containersResponse = kieServerClient.listContainers();
        assertThat(containersResponse.getType()).isEqualTo(ResponseType.SUCCESS);
        assertThat(containersResponse.getResult().getContainers()).hasSize(1);
        assertThat(containersResponse.getResult().getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
    }

    private void verifyOneServerTemplateWithContainer(String kieServerLocation, String containerId) {
        Collection<ServerTemplate> serverTemplates = kieControllerClient.listServerTemplates();
        assertThat(serverTemplates).as("Number of server templates differ.").hasSize(1);

        ServerTemplate serverTemplate = serverTemplates.iterator().next();
        assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
        assertThat(serverTemplate.getServerInstanceKeys().iterator().next().getUrl()).isEqualTo(kieServerLocation);
        assertThat(serverTemplate.getContainersSpec()).hasSize(1);
        assertThat(serverTemplate.getContainersSpec().iterator().next().getId()).isEqualTo(containerId);
    }

    private void assertRepositoryAndProjectExists(String repoName, String projectName) {
        Collection<RepositoryResponse> repositories = workbenchClient.getRepositories();
        assertThat(repositories.stream().anyMatch(n -> n.getName().equals(repoName))).as("Repository " + repoName + " not found.").isTrue();

        Collection<ProjectResponse> projects = workbenchClient.getProjects(repoName);
        assertThat(projects.stream().anyMatch(n -> n.getName().equals(projectName))).as("Project " + projectName + " not found.").isTrue();
    }

    private void scaleToZeroAndToOne(Deployment deployment) {
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(1);
        deployment.waitForScale();
    }
}

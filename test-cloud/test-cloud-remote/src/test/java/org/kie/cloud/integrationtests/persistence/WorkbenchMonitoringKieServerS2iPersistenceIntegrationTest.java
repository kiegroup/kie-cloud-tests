/*
 * Copyright 2019 JBoss by Red Hat.
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
package org.kie.cloud.integrationtests.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.client.KieServerControllerClient;

public class WorkbenchMonitoringKieServerS2iPersistenceIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<GenericScenario> {

    protected KieServicesClient kieServicesClient;
    protected KieServicesClient smartServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected UserTaskServicesClient taskServicesClient;
    protected KieServerControllerClient kieServerControllerClient;

    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + Kjar.DEFINITION.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private String repositoryName;
    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        repositoryName = gitProvider.createGitRepositoryWithPrefix("KieServerS2iJbpmRepository", ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        DeploymentSettings workbenchMonitoringSettings = deploymentScenarioFactory.getWorkbenchMonitoringSettingsBuilder()
                .build();
        DeploymentSettings kieServerS2ISettings = deploymentScenarioFactory.getKieServerHttpsS2ISettingsBuilder()
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .withSourceLocation(gitProvider.getRepositoryUrl(repositoryName), REPO_BRANCH, DEFINITION_PROJECT_NAME)
                .withSmartRouterConnection("rhpam-immutable-mon-smartrouter")
                //.withControllerConnection("rhpam-immutable-mon-rhpamcentrmon") //should not be hardcoded ! and connected to only smart router..
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withMonitoring(workbenchMonitoringSettings)
                .withKieServer(kieServerS2ISettings)
                .build();

        /*
        One mode:

        unmaneged- Kie Server S2I connected only to the smart router
        */
    }

    @Before
    public void setUp() {
        kieServerControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployments().get(0));
        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployments().get(0));
        taskServicesClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployments().get(0));

        smartServicesClient = KieServerClientProvider.getSmartRouterClient(deploymentScenario.getSmartRouterDeployments().get(0), DeploymentConstants.getControllerUser(), DeploymentConstants.getControllerPassword());
    }

    @After
    public void deleteRepo() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testContainerAfterExecServerS2IStart() {
        ServerTemplate[] serverTemplates = kieServerControllerClient.listServerTemplates().getServerTemplates();
        assertThat(serverTemplates).hasSize(1); // server template should be only one (for Smart router)

        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).isNotNull().hasSize(1);
        // check container from smart router...
        containers = smartServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).isNotNull().hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isNotNull().isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();
        assertThat(containerReleaseId.getGroupId()).isNotNull().isEqualTo(PROJECT_GROUP_ID);
        assertThat(containerReleaseId.getArtifactId()).isNotNull().isEqualTo(DEFINITION_PROJECT_NAME);
        assertThat(containerReleaseId.getVersion()).isNotNull().isEqualTo(DEFINITION_PROJECT_VERSION);

        Long processId = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.USERTASK);
        assertThat(processId).isNotNull();

        List<TaskSummary> tasks = taskServicesClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(tasks).isNotNull().hasSize(1);

        // Kill it here...
        deploymentScenario.getKieServerDeployments().forEach(ksd -> {
            ksd.deleteInstances(ksd.getInstances());
        });
        deploymentScenario.getKieServerDeployments().forEach(ksd -> {
            ksd.waitForScale();
        });


        taskServicesClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), Constants.User.YODA, null);

        ProcessInstance userTaskPi = processServicesClient.getProcessInstance(CONTAINER_ID, processId);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

//    @Test
    public void testHttps() {
        // not persistance test...
        deploymentScenario.getWorkbenchDeployments().forEach(wbDeployment -> {
            HttpsWorkbenchTestProvider.testLoginScreen(wbDeployment, false);
            HttpsWorkbenchTestProvider.testControllerOperations(wbDeployment, false);
        });
        deploymentScenario.getKieServerDeployments().forEach(kieServerDeployment -> {
            HttpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, false);
        });
    }

}

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
package org.kie.cloud.integrationtests.s2i;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.GenericScenario;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;

public class KieServerS2iHierarchicalIntegrationTest extends AbstractCloudIntegrationTest<GenericScenario> {

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected UserTaskServicesClient taskServicesClient;

    private static final Kjar USERTASK = Kjar.USERTASK;
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + USERTASK.toString();

    private static final String GIT_REPOSITORY_PREFIX = "KieServerS2iHierarchicalRepository";

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";
    private static final String GIT_CONTEXT_DIR = "multimodule-project";
    private static final String BUILT_KJAR_FOLDER = "usertask-project/target,signaltask-project/target";

    private String repositoryName;

    @Override
    protected GenericScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        repositoryName = gitProvider.createGitRepositoryWithPrefix(GIT_REPOSITORY_PREFIX, PROJECT_SOURCE_FOLDER);

        DeploymentSettings kieServerS2Isettings = deploymentScenarioFactory.getKieServerS2ISettingsBuilder()
                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                .withSourceLocation(gitProvider.getRepositoryUrl(repositoryName), REPO_BRANCH, GIT_CONTEXT_DIR, BUILT_KJAR_FOLDER)
                .build();

        return deploymentScenarioFactory.getGenericScenarioBuilder()
                .withKieServer(kieServerS2Isettings)
                .build();
    }

    @Before
    public void setUp() {
        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployments().get(0));
        taskServicesClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployments().get(0));
    }

    @After
    public void deleteRepo() {
        gitProvider.deleteGitRepository(repositoryName);
    }

    @Test
    public void testContainerAfterExecServerS2iStart() {
        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(containerReleaseId.getGroupId()).isEqualTo(USERTASK.getGroupId());
            softly.assertThat(containerReleaseId.getArtifactId()).isEqualTo(USERTASK.getName());
            softly.assertThat(containerReleaseId.getVersion()).isEqualTo(USERTASK.getVersion());
        });

        Long processId = processServicesClient.startProcess(CONTAINER_ID, USERTASK_PROCESS_ID);
        assertThat(processId).isNotNull();

        List<TaskSummary> tasks = taskServicesClient.findTasks(USER_YODA, 0, 10);
        assertThat(tasks).hasSize(1);

        taskServicesClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);

        ProcessInstance userTaskPi = processServicesClient.getProcessInstance(CONTAINER_ID, processId);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }
}

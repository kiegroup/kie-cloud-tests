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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
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
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.time.Constants;
import org.kie.cloud.utils.GitUtils;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
@Category(JBPMOnly.class)
public class KieServerS2iHierarchicalIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {

    private static final String REPOSITORY_NAME = generateNameWithPrefix("KieServerS2iHierarchicalRepository");

    private static final Logger logger = LoggerFactory.getLogger(KieServerS2iHierarchicalIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieDeploymentScenario<?> deploymentScenario;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        GitSettings gitSettings = GitSettings.fromProperties()
                                             .withRepository(REPOSITORY_NAME,
                                                             KieServerS2iHierarchicalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        try {
            KieDeploymentScenario<?> immutableKieServerWithDatabaseScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder()
                                                                                                       .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                                       .withGitSettings(gitSettings)
                                                                                                       .withSourceLocation(REPO_BRANCH, GIT_CONTEXT_DIR, BUILT_KJAR_FOLDER)
                                                                                                       .build();
            scenarios.add(new Object[] { "Immutable KIE Server Database S2I", immutableKieServerWithDatabaseScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server Database S2I is skipped.", ex);
        }

        try {
            KieDeploymentScenario<?> immutableKieServerScenario = deploymentScenarioFactory.getImmutableKieServerScenarioBuilder()
                                                                                           .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                           .withGitSettings(gitSettings)
                                                                                           .withSourceLocation(REPO_BRANCH, GIT_CONTEXT_DIR, BUILT_KJAR_FOLDER)
                                                                                           .build();
            scenarios.add(new Object[]{"Immutable KIE Server S2I", immutableKieServerScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server S2I is skipped.", ex);
        }

        return scenarios;
    }

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected UserTaskServicesClient taskServicesClient;

    private static final Kjar USERTASK = Kjar.USERTASK;
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + USERTASK.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";
    private static final String GIT_CONTEXT_DIR = "multimodule-project";
    private static final String BUILT_KJAR_FOLDER = "usertask-project/target,signaltask-project/target";

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenario;
    }

    @Before
    public void setUp() {
        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployments().get(0));
        taskServicesClient = KieServerClientProvider.getTaskClient(deploymentScenario.getKieServerDeployments().get(0));
    }

    @After
    public void deleteRepo() {
        GitUtils.deleteGitRepository(REPOSITORY_NAME, deploymentScenario);
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
            softly.assertThat(containerReleaseId.getArtifactId()).isEqualTo(USERTASK.getArtifactName());
            softly.assertThat(containerReleaseId.getVersion()).isEqualTo(USERTASK.getVersion());
        });

        Long processId = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.USERTASK);
        assertThat(processId).isNotNull();

        List<TaskSummary> tasks = taskServicesClient.findTasks(Constants.User.YODA, 0, 10);
        assertThat(tasks).hasSize(1);

        taskServicesClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), Constants.User.YODA, null);

        ProcessInstance userTaskPi = processServicesClient.getProcessInstance(CONTAINER_ID, processId);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }
}

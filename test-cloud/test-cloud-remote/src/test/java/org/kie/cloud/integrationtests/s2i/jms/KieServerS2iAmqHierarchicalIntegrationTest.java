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
package org.kie.cloud.integrationtests.s2i.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.s2i.KieServerS2iHierarchicalIntegrationTest;
import org.kie.cloud.provider.git.Git;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.time.Constants;
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
public class KieServerS2iAmqHierarchicalIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {

    private static final Logger logger = LoggerFactory.getLogger(KieServerS2iAmqHierarchicalIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieDeploymentScenario<?> deploymentScenario;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            KieDeploymentScenario<?> immutableKieServerWithDatabaseScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioBuilder()
                                                                                                       .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                                       .withSourceLocation(Git.getProvider().getRepositoryUrl(gitRepositoryName), REPO_BRANCH, GIT_CONTEXT_DIR, BUILT_KJAR_FOLDER)
                                                                                                       .build();
            scenarios.add(new Object[] { "Immutable KIE Server AMQ Database S2I", immutableKieServerWithDatabaseScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server AMQ Database S2I is skipped.", ex);
        }

        try {
            KieDeploymentScenario<?> immutableKieServerScenario = deploymentScenarioFactory.getImmutableKieServerAmqScenarioBuilder()
                                                                                           .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                           .withSourceLocation(Git.getProvider().getRepositoryUrl(gitRepositoryName), REPO_BRANCH, GIT_CONTEXT_DIR, BUILT_KJAR_FOLDER)
                                                                                           .build();
            scenarios.add(new Object[]{"Immutable KIE Server AMQ S2I", immutableKieServerScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server AMQ S2I is skipped.", ex);
        }

        return scenarios;
    }

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected UserTaskServicesClient taskServicesClient;

    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + Kjar.USERTASK.toString();

    private static final String GIT_REPOSITORY_PREFIX = "KieServerS2iHierarchicalRepository";

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";
    private static final String GIT_CONTEXT_DIR = "multimodule-project";
    private static final String BUILT_KJAR_FOLDER = "usertask-project/target,signaltask-project/target";

    private static String gitRepositoryName = Git.getProvider().createGitRepositoryWithPrefix(GIT_REPOSITORY_PREFIX, KieServerS2iHierarchicalIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenario;
    }

    @Before
    public void setUp() {
        AmqDeployment amqDeployment = deploymentScenario.getDeployments().stream()
                .filter(AmqDeployment.class::isInstance)
                .map(AmqDeployment.class::cast)
                .findFirst()
                .orElseThrow(()->new RuntimeException("No AMQ deployment founded."));

        kieServicesClient = KieServerClientProvider.getKieServerJmsClient(amqDeployment.getTcpSslUrl());
        processServicesClient = KieServerClientProvider.getProcessJmsClient(kieServicesClient);
        taskServicesClient = KieServerClientProvider.getTaskJmsClient(kieServicesClient);
    }

    @AfterClass
    public static void deleteRepo() {
        Git.getProvider().deleteGitRepository(gitRepositoryName);
    }

    @Test
    @Category(JBPMOnly.class)
    public void testContainerAfterExecServerS2iStart() {
        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(containerReleaseId.getGroupId()).isEqualTo(Kjar.USERTASK.getGroupId());
            softly.assertThat(containerReleaseId.getArtifactId()).isEqualTo(Kjar.USERTASK.getArtifactName());
            softly.assertThat(containerReleaseId.getVersion()).isEqualTo(Kjar.USERTASK.getVersion());
        });

        Long processId = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.USERTASK);
        assertThat(processId).isNotNull();

        List<TaskSummary> tasks = taskServicesClient.findTasks(DeploymentConstants.getAppUser(), 0, 10);
        assertThat(tasks).hasSize(1);

        taskServicesClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), DeploymentConstants.getAppUser(), null);

        ProcessInstance userTaskPi = processServicesClient.getProcessInstance(CONTAINER_ID, processId);
        assertThat(userTaskPi).isNotNull();
        assertThat(userTaskPi.getState()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }
}

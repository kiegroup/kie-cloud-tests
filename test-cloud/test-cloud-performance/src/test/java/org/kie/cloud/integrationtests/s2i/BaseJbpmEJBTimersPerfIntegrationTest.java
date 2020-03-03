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
package org.kie.cloud.integrationtests.s2i;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.junit.After;
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
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.category.Performance;
import org.kie.cloud.provider.git.Git;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.RunnableWrapper;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

@RunWith(Parameterized.class)
public abstract class BaseJbpmEJBTimersPerfIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario> {

    private static final String REPOSITORY_NAME = generateNameWithPrefix("KieServerS2iJbpmRepository");

    protected static final String MEMORY = "memory";
    protected static final String CPU = "cpu";
    protected static final List<Integer> ACTIVE_STATUS = Collections.singletonList(STATE_ACTIVE);
    protected static final List<Integer> COMPLETED_STATUS = Collections.singletonList(STATE_COMPLETED);

    protected static final Logger logger = LoggerFactory.getLogger(BaseJbpmEJBTimersPerfIntegrationTest.class);

    protected static final int PROCESSES_COUNT = Integer.parseInt(System.getProperty("processesCount", "5000"));
    protected static final double PERF_INDEX = Double.parseDouble(System.getProperty("perfIndex", "3.0"));
    protected static final String HEAP = System.getProperty("heap","4Gi");
    protected static final int SCALE_COUNT = Integer.parseInt(System.getProperty("scale", "1"));
    protected static final int REPETITIONS = Integer.parseInt(System.getProperty("repetitions", "1"));
    protected static final int REFRESH_INTERVAL = Integer.parseInt(System.getProperty("refreshInterval", "30"));
    protected static final int ROUTER_TIMEOUT = Integer.parseInt(System.getProperty("routerTimeout", "60"));
    protected static final String ROUTER_BALANCE = System.getProperty("routerBalance", "roundrobin");

    protected static final String ONE_TIMER_DURATION_PROCESS_ID = "timers-testing.OneTimerDate";

    protected static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + Kjar.DEFINITION.toString();

    protected static final String REPO_BRANCH = "master";
    protected static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";
    protected static final int MINIMUM_OFFSET = 45;

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario deploymentScenario;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario immutableKieServerWithDatabaseScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder()
                                                                                                                                                .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                                                                                .withSourceLocation(Git.getProvider().getRepositoryUrl(gitRepositoryName), REPO_BRANCH, DEFINITION_PROJECT_NAME)
                                                                                                                                                .withTimerServiceDataStoreRefreshInterval(Duration.ofSeconds(REFRESH_INTERVAL))
                                                                                                                                                .withKieServerMemoryLimit(HEAP)
                                                                                                                                                .build();
            for (int i=0;i<REPETITIONS;i++) {
                scenarios.add(new Object[] { "KIE Server HTTPS S2I", immutableKieServerWithDatabaseScenario });
            }
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server HTTPS S2I is skipped.", ex);
        }

        return scenarios;
    }

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected UserTaskServicesClient taskServicesClient;

    protected QueryServicesClient queryServicesClient;

    private static String gitRepositoryName = Git.getProvider().createGitRepository(REPOSITORY_NAME, BaseJbpmEJBTimersPerfIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

    protected List<String> pods = new ArrayList<String>();

    protected Map<String, Integer> completedHostNameDistribution;

    @Override
    protected WorkbenchRuntimeSmartRouterImmutableKieServerWithDatabaseScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenario;
    }

    @Before
    public void setUp() {
        // Scale Kie server to 0 to apply configuration changes.
        deploymentScenario.getKieServerDeployment().scale(0);
        deploymentScenario.getKieServerDeployment().waitForScale();

        Map<String, String> requests = new HashMap<String, String>();
        requests.put(CPU, System.getProperty("requests.cpu","1000m"));
        requests.put(MEMORY, System.getProperty("requests.memory","1Gi"));
        Map<String, String> limits = new HashMap<String, String>();
        limits.put(CPU, System.getProperty("limits.cpu","4000m"));
        limits.put(MEMORY, System.getProperty("limits.memory","4Gi"));
        deploymentScenario.getKieServerDeployment().setResources(requests, limits);

        if (SCALE_COUNT >= 1) {
            logger.info("starting to scale");
            scaleKieServerTo(SCALE_COUNT);
            logger.info("scaled to {} pods", SCALE_COUNT);
        } else {
            throw new RuntimeException("wrong scale parameter, should be equal or greater than 1");
        }

        deploymentScenario.getKieServerDeployment().setRouterTimeout(Duration.ofMinutes(ROUTER_TIMEOUT));
        deploymentScenario.getKieServerDeployment().setRouterBalance(ROUTER_BALANCE);

        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment(), Duration.ofMinutes(60).toMillis());

        logger.info("Setting timeout for kieServerClient to {}", Duration.ofMinutes(60).toMillis());

        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        queryServicesClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());
    }

    @After
    public void resetRouterTimeout() {
        deploymentScenario.getKieServerDeployment().resetRouterTimeout();
    }

    @AfterClass
    public static void deleteRepo() {
        Git.getProvider().deleteGitRepository(gitRepositoryName);
    }

    @Test
    @Category(Performance.class)
    public void testContainerAfterExecServerS2IStart() throws IOException {
        List<KieContainerResource> containers = kieServicesClient.listContainers().getResult().getContainers();
        assertThat(containers).isNotNull().hasSize(1);

        KieContainerResource container = containers.get(0);
        assertThat(container).isNotNull();
        assertThat(container.getContainerId()).isNotNull().isEqualTo(CONTAINER_ID);

        ReleaseId containerReleaseId = container.getResolvedReleaseId();
        assertThat(containerReleaseId).isNotNull();
        assertThat(containerReleaseId.getGroupId()).isNotNull().isEqualTo(PROJECT_GROUP_ID);
        assertThat(containerReleaseId.getArtifactId()).isNotNull().isEqualTo(DEFINITION_PROJECT_NAME);
        assertThat(containerReleaseId.getVersion()).isNotNull().isEqualTo(DEFINITION_PROJECT_VERSION);

        logger.info("============================= STARTING SCENARIO =============================");
        runSingleScenario();
        logger.info("============================= SCENARIO COMPLETE =============================");

        logger.info("============================= GATHERING STATISTICS =============================");
        gatherAndAssertStatistics();
        writeCSV();
        logger.info("============================= STATISTICS GATHERED =============================");
    }

    protected abstract void writeCSV() throws IOException;

    protected abstract void runSingleScenario();

    protected void startAndWaitForStartingThreads(int numberOfThreads, Duration duration, Integer iterations, Runnable runnable) {
       List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Thread t = new Thread(new RunnableWrapper(duration, iterations, runnable));
            t.start();
            threads.add(t);
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }

    protected Runnable getStartingRunnable(String containerId, String processId, Map<String, Object> parameters) {
        return () -> {
            try {
                long pid = processServicesClient.startProcess(containerId, processId, parameters);
                assertThat(pid).isNotNull();
            } catch (KieServicesHttpException e) {
                logger.error("There has been an error while starting processes", e);
                throw e;
            }
        };
    }

    protected void waitForAllProcessesToComplete(Duration waitForCompletionDuration) {
        BooleanSupplier completionCondition = () -> queryServicesClient.findProcessInstancesByStatus(ACTIVE_STATUS, 0, 1).isEmpty();
        TimeUtils.wait(waitForCompletionDuration, Duration.of(1, ChronoUnit.SECONDS), completionCondition);
    }

    private void gatherAndAssertStatistics() {
        int numberOfPages = 1 + (PROCESSES_COUNT / 5000);// including one additional page to check there are no more processes

        List<ProcessInstance> completedProcesses = new ArrayList<>(PROCESSES_COUNT);

        for (int i = 0; i < numberOfPages; i++) {
            List<ProcessInstance> response = queryServicesClient.findProcessInstancesByStatus(Collections.singletonList(org.jbpm.process.instance.ProcessInstance.STATE_COMPLETED), i, 5000);
            completedProcesses.addAll(response);
        }

        logger.info("Completed processes count: {}", completedProcesses.size());

        List<ProcessInstance> activeProcesses = queryServicesClient.findProcessInstancesByStatus(Collections.singletonList(org.jbpm.process.instance.ProcessInstance.STATE_ACTIVE), 0, 100);
        logger.info("Active processes count: {}", activeProcesses.size());

        assertThat(activeProcesses).isEmpty();

        assertThat(completedProcesses).hasSize(PROCESSES_COUNT);

        Map<String, Integer> startedHostNameDistribution = new HashMap<>();
        completedHostNameDistribution = new HashMap<>();

        for (String pod : pods) {
            completedHostNameDistribution.put(pod, 0);
            startedHostNameDistribution.put(pod, 0);
        }

        for (int i = 0; i < numberOfPages; i++) {
          for (String pod : pods) {
           int sizeCompleted = queryServicesClient.findProcessInstancesByVariableAndValue("hostName", pod, COMPLETED_STATUS, i, 5000).size();
           completedHostNameDistribution.put(pod, completedHostNameDistribution.get(pod) + sizeCompleted);

           int sizeStarted = queryOldValue(i, pod);
           startedHostNameDistribution.put(pod, startedHostNameDistribution.get(pod) + sizeStarted);
          }
        }

        logger.info("Processes were completed with this distribution: {}", completedHostNameDistribution);
        logger.info("Processes were started with this distribution: {}", startedHostNameDistribution);
    }

    private void scaleKieServerTo(int count) {
        deploymentScenario.getKieServerDeployment().scale(count);
        deploymentScenario.getKieServerDeployment().waitForScale();
        List<Instance> osInstances = deploymentScenario.getKieServerDeployment().getInstances();
        for (Instance i : osInstances) {
            pods.add(i.getName());
        }
    }

    private int queryOldValue(int page, String oldValue) {
        QueryFilterSpec spec = new QueryFilterSpecBuilder()
                .equalsTo("variableId", "hostName")
                .equalsTo("oldValue", oldValue)
                .get();

        return queryServicesClient.query("jbpmOldValueVarSearch", QueryServicesClient.QUERY_MAP_PI_WITH_VARS, spec, page, 5000, ProcessInstance.class).size();
    }

}

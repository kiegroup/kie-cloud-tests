/*
 * Copyright 2018 JBoss by Red Hat.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.provider.git.Git;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class KieServerS2iOptaplannerIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {

    private static final Logger logger = LoggerFactory.getLogger(KieServerS2iOptaplannerIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieDeploymentScenario<?> deploymentScenario;

    private static final String CLOUD_BALANCE_SOLVER_ID = "cloudsolver";
    private static final String CLOUD_BALANCE_SOLVER_CONFIG = "cloudbalance-solver.xml";

    private static final String CLASS_CLOUD_BALANCE = "org.kie.server.testing.CloudBalance";
    private static final String CLASS_CLOUD_COMPUTER = "org.kie.server.testing.CloudComputer";
    private static final String CLASS_CLOUD_PROCESS = "org.kie.server.testing.CloudProcess";
    private static final String CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE =
            "org.kie.server.testing.AddComputerProblemFactChange";
    private static final String CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE =
            "org.kie.server.testing.DeleteComputerProblemFactChange";
    private static final String CLASS_CLOUD_GENERATOR = "org.kie.server.testing.CloudBalancingGenerator";

    private static final Kjar DEPLOYED_KJAR = Kjar.CLOUD_BALANCE_SNAPSHOT;
    private static final ReleaseId CLOUD_BALANCE_RELEASE_ID = new ReleaseId(DEPLOYED_KJAR.getGroupId(), DEPLOYED_KJAR.getArtifactName(), DEPLOYED_KJAR.getVersion());
    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + DEPLOYED_KJAR.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private static String gitRepositoryName = Git.getProvider().createGitRepositoryWithPrefix("KieServerS2iOptaplannerRepository", KieServerS2iOptaplannerIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

    private KieContainer kieContainer;
    private SolverServicesClient solverClient;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            KieDeploymentScenario<?> immutableKieServerWithDatabaseScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerWithPostgreSqlScenarioBuilder()
                                                                                                       .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                                       .withSourceLocation(Git.getProvider().getRepositoryUrl(gitRepositoryName), REPO_BRANCH, DEPLOYED_KJAR.getArtifactName())
                                                                                                       .build();
            scenarios.add(new Object[] { "Immutable KIE Server Database S2I", immutableKieServerWithDatabaseScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server Database S2I is skipped.", ex);
        }

        try {
            KieDeploymentScenario<?> immutableKieServerScenario = deploymentScenarioFactory.getImmutableKieServerScenarioBuilder()
                                                                                           .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                                                                                           .withSourceLocation(Git.getProvider().getRepositoryUrl(gitRepositoryName), REPO_BRANCH, DEPLOYED_KJAR.getArtifactName())
                                                                                           .build();
            scenarios.add(new Object[] { "Immutable KIE Server S2I", immutableKieServerScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Immutable KIE Server S2I is skipped.", ex);
        }

        return scenarios;
    }

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenario;
    }

    @BeforeClass
    public static void buildKjar() {
        MavenDeployer.buildAndInstallMavenProject(
                KieServerS2iOptaplannerIntegrationTest.class.getResource("/kjars-sources/cloudbalance-snapshot").getFile(), new HashMap<>());
    }

    @Before
    public void setUp() throws ClassNotFoundException {
        kieContainer = KieServices.Factory.get().newKieContainer(CLOUD_BALANCE_RELEASE_ID);

        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(
                deploymentScenario.getKieServerDeployments().get(0),
                extraClasses(kieContainer));
        solverClient = kieServerClient.getServicesClient(SolverServicesClient.class);
    }

    @AfterClass
    public static void deleteRepo() {
        Git.getProvider().deleteGitRepository(gitRepositoryName);
    }

    @Test
    public void testExecuteSolver() throws Exception {
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID, CLOUD_BALANCE_SOLVER_CONFIG);
        assertThat(solverInstance).isNotNull();

        Object planningProblem = loadPlanningProblem(kieContainer, 5, 15);
        solverClient.solvePlanningProblem(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID, planningProblem);

        solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID); // wait for 15 seconds
        for (int i = 0; i < 5 && solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING; i++) {
            Thread.sleep(3000);
            solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID);
        }

        assertThat(solverInstance.getStatus()).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);
        assertThat(solverInstance.getScoreWrapper()).isNotNull();
        assertThat(solverInstance.getScoreWrapper().getScoreString()).isNotEmpty();
    }

    private Set<Class<?>> extraClasses(KieContainer kieContainer) throws ClassNotFoundException {
        Set<Class<?>> extra = new HashSet<>();

        extra.add(Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
        extra.add(Class.forName(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, true,
                kieContainer.getClassLoader()));

        return extra;
    }

    private Object loadPlanningProblem(KieContainer kieContainer, int computerListSize,
            int processListSize) throws NoSuchMethodException, ClassNotFoundException,
            IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> cbgc = kieContainer.getClassLoader().loadClass(CLASS_CLOUD_GENERATOR);
        Object cbgi = cbgc.newInstance();

        Method method = cbgc.getMethod("createCloudBalance",
                int.class,
                int.class);
        return method.invoke(cbgi,
                computerListSize,
                processListSize);
    }
}

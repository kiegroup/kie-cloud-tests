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

package org.kie.cloud.integrationtests.planner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.SolverServicesClient;

public class OptaplannerIntegrationTest extends AbstractCloudIntegrationTest<WorkbenchWithKieServerScenario> {

    private static final ReleaseId CLOUD_BALANCE_RELEASE_ID = new ReleaseId(
            PROJECT_GROUP_ID,
            CLOUD_BALANCE_PROJECT_SNAPSHOT_NAME,
            CLOUD_BALANCE_PROJECT_SNAPSHOT_VERSION);
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

    @Override protected WorkbenchWithKieServerScenario createDeploymentScenario(
            DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory
                .getWorkbenchWithKieServerScenarioBuilder()
                .withExternalMavenRepo(
                        MavenConstants.getMavenRepoUrl(),
                        MavenConstants.getMavenRepoUser(),
                        MavenConstants.getMavenRepoPassword())
                .build();
    }

    @Before
    public void setRouterTimeout() {
        deploymentScenario.getKieServerDeployment().setRouterTimeout(Duration.ofMinutes(3));
    }

    @Test
    public void testExecuteSolver() throws Exception {
        deploymentScenario.getKieServerDeployment().setRouterTimeout(Duration.ofMinutes(5));

        MavenDeployer.buildAndDeployMavenProject(
                ClassLoader.class.getResource("/kjars-sources/cloudbalance-snapshot").getFile());

        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(CLOUD_BALANCE_RELEASE_ID);

        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(
                deploymentScenario.getKieServerDeployment(),
                extraClasses(kieContainer));
        kieServerClient.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, CLOUD_BALANCE_RELEASE_ID));

        SolverServicesClient solverClient = kieServerClient.getServicesClient(SolverServicesClient.class);
        SolverInstance solverInstance = solverClient.createSolver(CONTAINER_ID,
                CLOUD_BALANCE_SOLVER_ID, CLOUD_BALANCE_SOLVER_CONFIG);
        Assertions.assertThat(solverInstance).isNotNull();

        Object planningProblem = loadPlanningProblem(kieContainer, 5, 15);
        solverClient.solvePlanningProblem(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID, planningProblem);

        solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID); // wait for 15 seconds
        for (int i = 0; i < 5 && solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING; i++) {
            Thread.sleep(3000);
            solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID);
        }
        solverInstance = solverClient.getSolver(CONTAINER_ID, CLOUD_BALANCE_SOLVER_ID);
        Assertions.assertThat(solverInstance.getStatus()).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);
        Assertions.assertThat(solverInstance.getScoreWrapper()).isNotNull();
        Assertions.assertThat(solverInstance.getScoreWrapper().getScoreString()).isNotNull();
        Assertions.assertThat(solverInstance.getScoreWrapper().getScoreString()).isNotEmpty();
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

/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.integrationtests.testproviders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.KieServerUtils;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.SolverServicesClient;

public class OptaplannerTestProvider {

    private static final ReleaseId CLOUD_BALANCE_RELEASE_ID = new ReleaseId(Kjar.CLOUD_BALANCE_SNAPSHOT.getGroupId(), Kjar.CLOUD_BALANCE_SNAPSHOT.getName(), Kjar.CLOUD_BALANCE_SNAPSHOT.getVersion());
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

    static {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/cloudbalance-snapshot").getFile());
    }

    public static void testExecuteSolver(KieServerDeployment kieServerDeployment) {
        kieServerDeployment.setRouterTimeout(Duration.ofMinutes(3));
        String containerId = "testExecuteSolver";

        KieContainer kieContainer = KieServices.Factory.get().newKieContainer(CLOUD_BALANCE_RELEASE_ID);
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment, extraClasses(kieContainer));
        KieServerUtils.createContainer(kieServerClient, new KieContainerResource(containerId, CLOUD_BALANCE_RELEASE_ID), Duration.ofMinutes(3));

        try {
            SolverServicesClient solverClient = kieServerClient.getServicesClient(SolverServicesClient.class);
            SolverInstance solverInstance = solverClient.createSolver(containerId,
                    CLOUD_BALANCE_SOLVER_ID, CLOUD_BALANCE_SOLVER_CONFIG);
            Assertions.assertThat(solverInstance).isNotNull();

            Object planningProblem = loadPlanningProblem(kieContainer, 5, 15);
            solverClient.solvePlanningProblem(containerId, CLOUD_BALANCE_SOLVER_ID, planningProblem);

            solverInstance = solverClient.getSolver(containerId, CLOUD_BALANCE_SOLVER_ID); // wait for 15 seconds
            for (int i = 0; i < 5 && solverInstance.getStatus() == SolverInstance.SolverStatus.SOLVING; i++) {
                Thread.sleep(3000);
                solverInstance = solverClient.getSolver(containerId, CLOUD_BALANCE_SOLVER_ID);
            }

            solverInstance = solverClient.getSolver(containerId, CLOUD_BALANCE_SOLVER_ID);
            Assertions.assertThat(solverInstance.getStatus()).isEqualTo(SolverInstance.SolverStatus.NOT_SOLVING);
            Assertions.assertThat(solverInstance.getScoreWrapper()).isNotNull();
            Assertions.assertThat(solverInstance.getScoreWrapper().getScoreString()).isNotNull();
            Assertions.assertThat(solverInstance.getScoreWrapper().getScoreString()).isNotEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for solver.", e);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Interrupted while loading planning problem.", e);
        } finally {
            kieServerDeployment.resetRouterTimeout();
            kieServerClient.disposeContainer(containerId);
        }
    }

    private static Set<Class<?>> extraClasses(KieContainer kieContainer) {
        Set<Class<?>> extra = new HashSet<>();

        try {
            extra.add(Class.forName(CLASS_CLOUD_BALANCE, true, kieContainer.getClassLoader()));
            extra.add(Class.forName(CLASS_CLOUD_COMPUTER, true, kieContainer.getClassLoader()));
            extra.add(Class.forName(CLASS_CLOUD_PROCESS, true, kieContainer.getClassLoader()));
            extra.add(Class.forName(CLASS_ADD_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
            extra.add(Class.forName(CLASS_DELETE_COMPUTER_PROBLEM_FACT_CHANGE, true, kieContainer.getClassLoader()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error while instantiating solver classes.", e);
        }

        return extra;
    }

    private static Object loadPlanningProblem(KieContainer kieContainer, int computerListSize,
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

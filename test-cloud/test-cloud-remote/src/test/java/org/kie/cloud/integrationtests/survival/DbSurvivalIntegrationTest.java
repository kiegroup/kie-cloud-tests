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
package org.kie.cloud.integrationtests.survival;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.KieServerWithDatabaseScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.maven.constants.MavenConstants;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class DbSurvivalIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieServerWithDatabaseScenario> {

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieServerWithDatabaseScenario kieServerScenario;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            KieServerWithDatabaseScenario kieServerMySqlScenario = deploymentScenarioFactory.getKieServerWithMySqlScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
            scenarios.add(new Object[]{"KIE Server + MySQL", kieServerMySqlScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server + MySQL is skipped.", ex);
        }

        try {
            KieServerWithDatabaseScenario kieServerPostgreSqlScenario = deploymentScenarioFactory.getKieServerWithPostgreSqlScenarioBuilder()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .build();
            scenarios.add(new Object[]{"KIE Server + PostgreSQL", kieServerPostgreSqlScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server + PostgreSQL is skipped.", ex);
        } 

        return scenarios;
    }

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected QueryServicesClient queryServicesClient;

    private static final Logger logger = LoggerFactory.getLogger(DbSurvivalIntegrationTest.class);

    @Override
    protected KieServerWithDatabaseScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return kieServerScenario;
    }

    @BeforeClass
    public static void deployMavenProject() {
        MavenDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project-snapshot").getFile());
    }

    @Before
    public void setUp() {
        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());
        queryServicesClient = KieServerClientProvider.getQueryClient(deploymentScenario.getKieServerDeployment());

        kieServicesClient.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, new ReleaseId(PROJECT_GROUP_ID, DEFINITION_PROJECT_SNAPSHOT_NAME, DEFINITION_PROJECT_SNAPSHOT_VERSION)));
    }

    @Test
    public void reconnectionDbTest() {
        logger.debug("Start process instance");
        Long signalPid = processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        assertThat(signalPid).isNotNull().isGreaterThan(0L);
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        scaleDatabaseTo(0);

        logger.debug("Try to get process instances");
        assertThatThrownBy(() -> {
            processServicesClient.startProcess(CONTAINER_ID, Constants.ProcessId.SIGNALTASK);
        }).isInstanceOf(KieServicesException.class);

        scaleDatabaseTo(1);

        waitForKieServerResponse();

        logger.debug("Check started processes");
        assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull().hasSize(1);

        logger.debug("Send signal and complete process");
        processServicesClient.signal(CONTAINER_ID, Constants.Signal.SIGNAL_NAME, null);

        logger.debug("Check that prcoess is completed");
        assertThat(queryServicesClient.findProcessInstanceById(signalPid).getState()).isEqualTo(ProcessInstance.STATE_COMPLETED);

    }

    private void scaleDatabaseTo(int count) {
        logger.debug("Scale Database to " + count);
        deploymentScenario.getDatabaseDeployment().scale(count);
        deploymentScenario.getDatabaseDeployment().waitForScale();
    }

    private void waitForKieServerResponse() {
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(30);

        while (LocalDateTime.now().isBefore(endTime)) {
            try {
                assertThat(queryServicesClient.findProcessInstances(0, 10)).isNotNull();
                return;
            } catch (KieServicesException e) {
                //ok
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Kie Server response.", e);
            }
        }
        throw new RuntimeException("Timeout while waiting for Kie Server.");
    }
}

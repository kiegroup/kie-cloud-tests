/*
 * Copyright 2022 JBoss by Red Hat.
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;

public class KieServerRepositoryPersistenceIntegrationTest extends AbstractCloudIntegrationTest {

    private static WorkbenchKieServerScenario deploymentScenario;
    private static ProcessTestProvider processTestProvider;
    private static String CONTAINER_ID = "persistenceProcess";

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                            .withInternalMavenRepo()
                            .withReposPersistence()
                            .build();
        ScenarioDeployer.deployScenario(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void test() {
        KieServerDeployment kieServerDeployment = deploymentScenario.getKieServerDeployment();
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);

        ServiceResponse<KieContainerResource> createContainer = kieServerClient.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, new ReleaseId(Kjar.DEFINITION_SNAPSHOT.getGroupId(),
                                                                                                                                                                 Kjar.DEFINITION_SNAPSHOT.getArtifactName(),
                                                                                                                                                                 Kjar.DEFINITION_SNAPSHOT.getVersion())));
        KieServerAssert.assertSuccess(createContainer);
        kieServerDeployment.waitForContainerRespin();
        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);

        MavenRepositoryDeployment nexusDeployment = deploymentScenario.getMavenRepositoryDeployment();
        nexusDeployment.scale(0);
        nexusDeployment.waitForScale();

        kieServerDeployment.scale(0);
        kieServerDeployment.waitForScale();
        kieServerDeployment.scale(1);
        kieServerDeployment.waitForScale();

        processTestProvider.testExecuteProcesses(deploymentScenario.getKieServerDeployment(), CONTAINER_ID);
    }
}

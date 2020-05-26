/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.integrationtests.persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.integrationtests.testproviders.PersistenceTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

public class WorkbenchAdminUserPersistenceIntegrationTest extends AbstractCloudIntegrationTest {

    private static WorkbenchKieServerScenario deploymentScenario;

    private static PersistenceTestProvider persistenceTestProvider;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .withInternalMavenRepo()
                .build();
        deploymentScenario.setLogFolderName(WorkbenchAdminUserPersistenceIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        persistenceTestProvider = PersistenceTestProvider.create();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testAdminUserPasswordChange() {
        persistenceTestProvider.testAdminUserPasswordChange(deploymentScenario);
    }

    @Test
    public void testAdminUserNameAndPasswordChange() {
        persistenceTestProvider.testAdminUserNameAndPasswordChange(deploymentScenario);
    }
}

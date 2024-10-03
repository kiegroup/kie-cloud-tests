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

package org.kie.cloud.integrationtests.smoke;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.KieServerWithDatabaseScenario;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

@Category(Smoke.class)
public class KieServerWithPostgreSqlScenarioIntegrationTest extends AbstractCloudIntegrationTest {

    private static KieServerWithDatabaseScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;

    @BeforeClass
    public static void initializeDeployment() {
        try {
            deploymentScenario = deploymentScenarioFactory.getKieServerWithPostgreSqlScenarioBuilder()
                    .withInternalMavenRepo(false)
                    .build();
            deploymentScenario.setLogFolderName(KieServerWithPostgreSqlScenarioIntegrationTest.class.getSimpleName());
            ScenarioDeployer.deployScenario(deploymentScenario);

            // Setup test providers
            fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
            processTestProvider = ProcessTestProvider.create(deploymentScenario);
            httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerDeployment());
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerDeployment());
    }

    @Test
    public void testKieServerHttps() {
        httpsKieServerTestProvider.testKieServerInfo(deploymentScenario.getKieServerDeployment(), false);
        httpsKieServerTestProvider.testDeployContainer(deploymentScenario.getKieServerDeployment(), false);
    }
}

/*
 * Copyright 2021 JBoss by Red Hat.
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
package org.kie.cloud.integrationtests.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.scenario.KieServerScenario;
import org.kie.cloud.integrationtests.testproviders.KieServerDependenciesTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

public class KieServerDependenciesIntegrationTest extends AbstractCloudIntegrationTest {

    private static KieServerScenario deploymentScenario;
    private static KieServerDependenciesTestProvider dependenciesTestProvider;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getKieServerScenarioBuilder().build();
        ScenarioDeployer.deployScenario(deploymentScenario);
        dependenciesTestProvider = KieServerDependenciesTestProvider.create();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testDependenciesExist() {
        dependenciesTestProvider.testDependenciesFolderNotEmpty(deploymentScenario);
    }

    @Test
    public void testJBPMClustering() {
        dependenciesTestProvider.testClusterDependencyExists(deploymentScenario);
        dependenciesTestProvider.testClusterDependencyVersion(deploymentScenario);
    }

    @Test
    public void testJBPMKafka() {
        dependenciesTestProvider.testKafkaDependencyExists(deploymentScenario);
        dependenciesTestProvider.testKafkaDependencyVersion(deploymentScenario);
    }

}
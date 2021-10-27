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

import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.KieServerScenario;
import org.kie.cloud.integrationtests.testproviders.KieServerDependenciesTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KieServerDependenciesIntegrationTest extends AbstractCloudIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(KieServerDependenciesIntegrationTest.class);

    private DeploymentScenarioBuilderFactory deploymentScenarioFactory;
    private KieServerScenario deploymentScenario;
    private KieServerDependenciesTestProvider provider;

    @Before
    public void setUp() {
        deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();
        deploymentScenario = deploymentScenarioFactory.getKieServerScenarioBuilder()
                .withInternalMavenRepo(false)
                .build();
        ScenarioDeployer.deployScenario(deploymentScenario);
        provider = KieServerDependenciesTestProvider.create();
    }

    @Test
    public void testDependenciesExist() {
        provider.testDependenciesFolderNotEmpty(deploymentScenario);
    }

    @Test
    public void testJBPMClustering() {
        provider.testClusterDependencyExists(deploymentScenario);
        provider.testClusterDependencyVersion(deploymentScenario);
    }

    @Test
    public void testJBPMKafka() {
        provider.testKafkaDependencyExists(deploymentScenario);
        provider.testKafkaDependencyVersion(deploymentScenario);
    }

}
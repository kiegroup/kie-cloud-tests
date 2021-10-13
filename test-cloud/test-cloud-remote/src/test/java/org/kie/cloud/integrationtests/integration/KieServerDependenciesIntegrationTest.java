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

import java.util.List;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.scenario.KieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.server.client.KieServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class KieServerDependenciesIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieServerScenario> {

    private static final Logger logger = LoggerFactory.getLogger(KieServerDependenciesIntegrationTest.class);

    private DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

    private KieServicesClient kieServicesClient;

    private KieServerDeployment kieServerDeployment;

    @Override
    protected KieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getKieServerScenarioBuilder()
                                        .withInternalMavenRepo(false)
                                        .build();
    }

    @Before
    public void setUp() {
        kieServerDeployment = deploymentScenario.getKieServerDeployments().get(0);
        kieServicesClient = KieServerClientProvider.getKieServerClient(kieServerDeployment);
    }

    @Test
    public void testDependenciesExist() {
        List<String> instanceNames = kieServerDeployment.getInstances().stream().map(Instance::getName).collect(Collectors.toList());
        OpenShiftBinary oc = OpenShifts.masterBinary(deploymentScenario.getNamespace());
        String[] args = {"rsh", instanceNames.get(0), "ls", "/opt/kie/dependencies"};
        String dependencies = oc.execute(args);
        logger.info("Found following dependencies: " + dependencies);
        logger.info("Container info:" + kieServicesClient.getContainerInfo(CONTAINER_ID).toString());
        assertThat(dependencies).isNotEmpty();
    }
}
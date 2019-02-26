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

package org.kie.cloud.tests.common;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.DeploymentScenario;

public abstract class AbstractMethodIsolatedCloudIntegrationTest<T extends DeploymentScenario<?>> extends AbstractCloudIntegrationTest {

    protected T deploymentScenario;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void initializeDeployment() {
        deploymentScenario = createDeploymentScenario(AbstractCloudIntegrationTest.deploymentScenarioFactory);
        deploymentScenario.setLogFolderName(testName.getMethodName());

        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @After
    public void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    protected abstract T createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory);
}

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

package org.kie.cloud.integrationtests;

import org.junit.After;
import org.junit.Before;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.GitProviderFactory;

public abstract class AbstractCloudIntegrationTest<T extends DeploymentScenario> {

    private final DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

    protected final GitProvider gitProvider = GitProviderFactory.getGitProvider();
    protected T deploymentScenario;

    @Before
    public void initializeDeployment() {
        deploymentScenario = createDeploymentScenario(deploymentScenarioFactory);
        deploymentScenario.deploy();
    }

    @After
    public void cleanEnvironment() {
        deploymentScenario.undeploy();
        gitProvider.deleteGitRepository(deploymentScenario.getNamespace());
    }

    protected abstract T createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory);
}

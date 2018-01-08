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
import org.junit.Assume;
import org.junit.Before;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.GitProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCloudIntegrationTest<T extends DeploymentScenario> {

    protected static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    protected static final String DEFINITION_PROJECT_NAME = "definition-project";
    protected static final String DEFINITION_PROJECT_VERSION = "1.0.0.Final";

    protected static final String DEFINITION_PROJECT_SNAPSHOT_NAME = "definition-project-snapshot";
    protected static final String DEFINITION_PROJECT_SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String CLOUD_BALANCE_PROJECT_SNAPSHOT_NAME = "cloudbalance-snapshot";
    protected static final String CLOUD_BALANCE_PROJECT_SNAPSHOT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String TIMER_PROJECT_NAME = "timer-project";
    protected static final String TIMER_PROJECT_VERSION = "1.0.0.Final";

    protected static final String RULE_PROJECT_NAME = "rule-project";
    protected static final String RULE_PROJECT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String HELLO_RULES_PROJECT_NAME = "hello-rules-snapshot";
    protected static final String HELLO_RULES_PROJECT_VERSION = "1.0.0-SNAPSHOT";

    protected static final String CONTAINER_ID = "cont-id";
    protected static final String CONTAINER_ALIAS = "cont-alias";

    protected static final String USER_YODA = "yoda";

    protected static final String USERTASK_PROCESS_ID = "definition-project.usertask";
    protected static final String UPDATED_USERTASK_PROCESS_ID = "definition-project.updated-usertask";
    protected static final String SIGNALTASK_PROCESS_ID = "definition-project.signaltask";
    protected static final String SIGNALUSERTASK_PROCESS_ID = "definition-project.signalusertask";
    protected static final String LONG_SCRIPT_PROCESS_ID = "definition-project.longScript";
    protected static final String SIMPLE_RULEFLOW_PROCESS_ID = "simple-ruleflow";
    protected static final String LOG_PROCESS_ID = "definition-project.logProcess";

    protected static final String SIGNAL_NAME = "signal1";
    protected static final String SIGNAL_2_NAME = "signal2";

    protected static final String WORKBENCH_LOGIN_SCREEN_TEXT = "Sign In";

    protected static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    protected static final String ORGANIZATION_UNIT_SECOND_NAME = "myOrgUnitTwo";
    protected static final String REPOSITORY_NAME = "myRepo";

    protected static final String ORGANIZATIONAL_UNIT_REST_REQUEST = "rest/organizationalunits";
    protected static final String KIE_SERVER_INFO_REST_REQUEST_URL = "services/rest/server";
    protected static final String KIE_CONTAINER_REQUEST_URL = "services/rest/server/containers";

    private static final int SCENARIO_DEPLOYMENT_ATTEMPTS = 3;

    private static final Logger logger = LoggerFactory.getLogger(AbstractCloudIntegrationTest.class);

    private final DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

    protected final GitProvider gitProvider = GitProviderFactory.getGitProvider();
    protected T deploymentScenario;

    @Before
    public void initializeDeployment() {
        deploymentScenario = createDeploymentScenario(deploymentScenarioFactory);

        boolean isDeployed = false;
        for (int i = 0; i < SCENARIO_DEPLOYMENT_ATTEMPTS && !isDeployed; i++) {
            isDeployed = deployScenario();
        }
    }

    @After
    public void cleanEnvironment() {
        if (deploymentScenario != null) {
            deploymentScenario.undeploy();

            if (gitProvider != null) {
                gitProvider.deleteGitRepository(deploymentScenario.getNamespace());
            }
        }
    }

    /**
     * @return True if deployment is successful.
     */
    private boolean deployScenario() {
        try {
            deploymentScenario.deploy();
            return true;
        } catch (MissingResourceException e) {
            logger.warn("Skipping test because of missing resource.", e);
            Assume.assumeNoException(e);
        } catch (DeploymentTimeoutException e) {
            logger.warn("Scenario didn't start in defined timeout, undeploying.", e);
            cleanEnvironment();
        }

        return false;
    }

    protected abstract T createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory);
}

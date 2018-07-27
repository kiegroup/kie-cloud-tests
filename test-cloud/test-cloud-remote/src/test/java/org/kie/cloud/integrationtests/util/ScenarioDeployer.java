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

package org.kie.cloud.integrationtests.util;

import org.junit.Assume;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.MissingResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioDeployer {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioDeployer.class);

    private static final int SCENARIO_DEPLOYMENT_ATTEMPTS = 3;

    public static void undeployScenario(DeploymentScenario deploymentScenario) {
        if (deploymentScenario != null) {
            deploymentScenario.undeploy();
        }
    }

    public static void deployScenario(DeploymentScenario deploymentScenario) {
        boolean isDeployed = false;
        for (int i = 0; i < SCENARIO_DEPLOYMENT_ATTEMPTS && !isDeployed; i++) {
            isDeployed = attemptToDeployScenario(deploymentScenario);
        }
    }

    /**
     * @return True if deployment is successful.
     */
    private static boolean attemptToDeployScenario(DeploymentScenario deploymentScenario) {
        try {
            deploymentScenario.deploy();
            return true;
        } catch (MissingResourceException e) {
            logger.warn("Skipping test because of missing resource.", e);
            Assume.assumeNoException(e);
        } catch (DeploymentTimeoutException e) {
            logger.warn("Scenario didn't start in defined timeout, undeploying.", e);
            undeployScenario(deploymentScenario);
        }

        return false;
    }
}

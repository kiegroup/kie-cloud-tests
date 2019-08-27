/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.hacep;
import cz.xtf.core.http.Https;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.HACepDeployment;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;

public class HACEPSmokeIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<HACepScenario> {

    private static final String HEALTH_URL = "/liveness";

    @Override
    protected HACepScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getHACepScenarioBuilder().build();
    }

    @Test
    public void testHACepHealth() throws InterruptedException {
        final String haCEPURL = ((HACepDeployment) deploymentScenario.getDeployments().get(0)).getUrl().toString();
        final int responseCode = Https.getCode(haCEPURL + HEALTH_URL);
        Assertions.assertThat(responseCode).isEqualTo(200);
    }
}

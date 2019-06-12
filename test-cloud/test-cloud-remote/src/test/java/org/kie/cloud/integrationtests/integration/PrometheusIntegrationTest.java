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

package org.kie.cloud.integrationtests.integration;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import cz.xtf.core.http.Https;
import cz.xtf.core.waiting.SupplierWaiter;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.PrometheusDeployment;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;

public class PrometheusIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchKieServerScenario> {

    private static final String PROMETHEUS_KIE_SERVER_STARTUP_TIME_SUFFIX = "/api/v1/query?query=kie_server_start_time";

    @Override
    protected WorkbenchKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder()
                                        .withPrometheusMonitoring()
                                        .build();
    }

    @Test
    public void testPrometheusIntegration() {
        PrometheusDeployment prometheusDeployment = deploymentScenario.getPrometheusDeployment().orElseThrow(() -> new RuntimeException("Prometheus deployment not available."));
        String prometheusKieServerStartupTimeUrl = prometheusDeployment.getUrl().toExternalForm() + PROMETHEUS_KIE_SERVER_STARTUP_TIME_SUFFIX;

        Https.doesUrlReturnOK(prometheusKieServerStartupTimeUrl).interval(TimeUnit.SECONDS, 30).waitFor();

        Function<String, Boolean> doesResponseContainsStrings = response -> {
            return response.contains("kieserver") && response.contains("version") && response.contains("location") && response.contains("value") && response.contains("kie_server_start_time");
        };
        new SupplierWaiter<String>(() -> Https.getContent(prometheusKieServerStartupTimeUrl), doesResponseContainsStrings).reason("Waiting for Prometheus REST response to contain Kie server start time.").timeout(TimeUnit.MINUTES, 1L).waitFor();
    }
}

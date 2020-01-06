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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.api.scenario.builder.HACepScenarioBuilder;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.hacep.sample.kjar.StockTickEvent;
import org.kie.remote.CommonConfig;
import org.kie.remote.RemoteFactHandle;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.TopicsConfig;

public class HACEPKjarUpdateIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<HACepScenario> {

    private static final String ENABLE_UPDATABLE_KJAR_PROPERTY = "UPDATABLEKJAR";
    private static final String KJAR_GAV_PROPERTY = "KJARGAV";
    private static final String KJAR1_FOLDER = "/kjars-sources/kjar1";
    private static final String KJAR2_FOLDER = "/kjars-sources/kjar2";
    private static final String KJAR1_GAV = "org.kie.cloud:test-hacep-cloud-kjar:1.0.0-SNAPSHOT";
    private static final String KJAR2_GAV = "org.kie.cloud:test-hacep-cloud-kjar:2.0.0-SNAPSHOT";

    private static final String FACT_KEY = "result";
    private static final String KJAR1_RESULT = "value1";
    private static final String KJAR2_RESULT = "value2";

    @Override
    protected HACepScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        final HACepScenarioBuilder haCepScenarioBuilder = deploymentScenarioFactory.getHACepScenarioBuilder();

        haCepScenarioBuilder.withInternalMavenRepo();

        final List<String> kjars = new ArrayList<>();
        kjars.add(KJAR1_FOLDER);
        kjars.add(KJAR2_FOLDER);
        haCepScenarioBuilder.setKjars(kjars);

        final Map<String, String> additionalEnvironmentParameters = new HashMap<>();
        additionalEnvironmentParameters.put(ENABLE_UPDATABLE_KJAR_PROPERTY, "true");
        additionalEnvironmentParameters.put(KJAR_GAV_PROPERTY, KJAR1_GAV);
        haCepScenarioBuilder.setSpringDeploymentEnvironmentVariables(additionalEnvironmentParameters);

        return haCepScenarioBuilder.build();
    }

    @Test
    public void testInstallFromKjar() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig)) {
            final RemoteFactHandle<Map<String, String>> factHandle = producer.insert(new HashMap<>());

            final CompletableFuture<Long> fireAllRulesFuture = producer.fireAllRules();
            fireAllRulesFuture.get();

            final CompletableFuture<Map<String, String>> factFuture = producer.getObject(factHandle);
            Map<String, String> retrievedFact = factFuture.get();
            Assertions.assertThat(retrievedFact).isNotNull();
            Assertions.assertThat(retrievedFact).containsKeys(FACT_KEY);
            Assertions.assertThat(retrievedFact.get(FACT_KEY)).isEqualTo(KJAR1_RESULT);
        }
    }

    @Test
    public void testUpdateKjar() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig)) {
            final CompletableFuture<Boolean> updateKjarFuture = producer
                    .updateKJarGAV(KJAR2_GAV);
            final boolean updateKjarResult = updateKjarFuture.get();
            Assertions.assertThat(updateKjarResult).isTrue();

            final RemoteFactHandle<Map<String, String>> factHandle = producer.insert(new HashMap<>());

            final CompletableFuture<Long> fireAllRulesFuture = producer.fireAllRules();
            fireAllRulesFuture.get();

            final CompletableFuture<Map<String, String>> factFuture = producer.getObject(factHandle);
            Map<String, String> retrievedFact = factFuture.get();
            Assertions.assertThat(retrievedFact).isNotNull();
            Assertions.assertThat(retrievedFact).containsKeys(FACT_KEY);
            Assertions.assertThat(retrievedFact.get(FACT_KEY)).isEqualTo(KJAR2_RESULT);
        }
    }
}

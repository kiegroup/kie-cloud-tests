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

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.hacep.sample.kjar.StockTickEvent;
import org.kie.remote.CommonConfig;
import org.kie.remote.RemoteFactHandle;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.RemoteStreamingKieSession;
import org.kie.remote.TopicsConfig;
import org.kie.remote.impl.RemoteFactHandleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HACEPRuleEngineIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<HACepScenario> {

    private static final Logger logger = LoggerFactory.getLogger(HACEPRuleEngineIntegrationTest.class);

    @Override
    protected HACepScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getHACepScenarioBuilder().build();
    }

    @Test
    public void testInsertRetrieveFact() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig)) {
            final StockTickEvent fact = new StockTickEvent("RHT",
                                                             ThreadLocalRandom.current().nextLong(80,
                                                                                                  100));
            final RemoteFactHandle<StockTickEvent> factHandle = producer.insert(fact);
            final CompletableFuture<StockTickEvent> futureRetrievedFact = producer.getObject(factHandle);
            final StockTickEvent retrievedFact = futureRetrievedFact.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT,
                                                                         TimeUnit.SECONDS);
            Assertions.assertThat(retrievedFact).isNotNull();
        }
    }

    @Test
    public void testInsertMultipleFacts() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());
        final int numberOfFacts = 100;

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig)) {
            for (int i = 0; i < numberOfFacts; i++) {
                StockTickEvent stockTickEvent = new StockTickEvent("RHT",
                                                           ThreadLocalRandom.current().nextLong(80,
                                                                                                100));
                producer.insert(stockTickEvent);
            }

            final CompletableFuture<Long> factCountFuture = producer.getFactCount();
            final long factCount = factCountFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT,
                                                       TimeUnit.SECONDS);
            Assertions.assertThat(factCount).isEqualTo(numberOfFacts);
        }
    }

    @Test
    public void testFireRules() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());
        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig)) {
            StockTickEvent stockTickEvent = new StockTickEvent("RHT",
                                                               ThreadLocalRandom.current().nextLong(80,
                                                                                                    100));
            final RemoteFactHandle<StockTickEvent> factHandle = producer.insert(stockTickEvent);

            final CompletableFuture<StockTickEvent> futureFactRetrievedBeforeRulesFired = producer.getObject(factHandle);
            final StockTickEvent retrievedFactBeforeRulesFired =
                    futureFactRetrievedBeforeRulesFired.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT, TimeUnit.SECONDS);
            Assertions.assertThat(retrievedFactBeforeRulesFired).isNotNull();
            Assertions.assertThat(retrievedFactBeforeRulesFired.isProcessed()).isFalse();

            CompletableFuture<Long> futureFiredRules = producer.fireAllRules();
            final long firedRules = futureFiredRules.get();
            Assertions.assertThat(firedRules).isEqualTo(1);

            final CompletableFuture<StockTickEvent> futureFactRetrievedAfterRulesFired = producer.getObject(factHandle);
            final StockTickEvent retrievedFactAfterRulesFired =
                    futureFactRetrievedAfterRulesFired.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT, TimeUnit.SECONDS);
            Assertions.assertThat(retrievedFactAfterRulesFired).isNotNull();
            Assertions.assertThat(retrievedFactAfterRulesFired.isProcessed()).isTrue();
        }

    }
}

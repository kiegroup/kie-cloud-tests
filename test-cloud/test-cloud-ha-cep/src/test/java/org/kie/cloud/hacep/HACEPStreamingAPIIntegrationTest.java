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
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.hacep.core.InfraFactory;
import org.kie.hacep.sample.kjar.StockTickEvent;
import org.kie.remote.CommonConfig;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.RemoteStreamingKieSession;
import org.kie.remote.TopicsConfig;
import org.kie.remote.impl.RemoteKieSessionImpl;
import org.kie.remote.impl.RemoteStreamingKieSessionImpl;
import org.kie.remote.impl.producer.Producer;
import org.kie.remote.util.KafkaRemoteUtil;

public class HACEPStreamingAPIIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<HACepScenario> {

    @Override
    protected HACepScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getHACepScenarioBuilder().build();
    }

    @Test
    public void testStreamingAPI() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(HACEPTestsUtils.getProperties());

        final Producer prod = InfraFactory.getProducer(false);
        try (RemoteStreamingKieSession producer = new RemoteStreamingKieSessionImpl(connectionProperties, topicsConfig, KafkaRemoteUtil.getListener(connectionProperties, false), prod)) {
            StockTickEvent stockTickEvent = new StockTickEvent("RHT",
                                                               ThreadLocalRandom.current().nextLong(80,
                                                                                                        100));
            producer.insert(stockTickEvent);

            final CompletableFuture<Long> factCountFuture = producer.getFactCount();
            final long factCount = factCountFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT,
                                                       TimeUnit.SECONDS);
            Assertions.assertThat(factCount).isEqualTo(1);
        }
    }
}

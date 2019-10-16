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

import io.fabric8.kubernetes.api.model.Pod;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.impl.ProjectImpl;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.hacep.sample.kjar.StockTickEvent;
import org.kie.remote.CommonConfig;
import org.kie.remote.RemoteFactHandle;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.TopicsConfig;

public class HACEPFailoverIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<HACepScenario> {

    @Override
    protected HACepScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getHACepScenarioBuilder().build();
    }

    @Test
    public void testLeaderFailoverFactCountTest() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());

        final int numberOfFacts = 10;

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig);
            Project project = new ProjectImpl(deploymentScenario.getNamespace())) {

            for (int i = 0; i < numberOfFacts; i++) {
                StockTickEvent stockTickEvent = new StockTickEvent("RHT",
                                                                   ThreadLocalRandom.current().nextLong(80,
                                                                                                        100));
                producer.insert(stockTickEvent);
            }

            CompletableFuture<Long> factCountFuture = producer.getFactCount();
            long factCount = factCountFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT,
                                                       TimeUnit.SECONDS);
            Assertions.assertThat(factCount).isEqualTo(numberOfFacts);

            final Pod leaderPod = HACEPTestsUtils.leaderPod(project);
            project.getOpenShift().deletePod(leaderPod);
            deploymentScenario.getDeployments().get(0).waitForScale();

            factCountFuture = producer.getFactCount();
            factCount = factCountFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT,
                                            TimeUnit.SECONDS);
            Assertions.assertThat(factCount).isEqualTo(numberOfFacts);
        }
    }

    @Test
    public void testRulesAreEvaluatedByReplicas() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig);
             Project project = new ProjectImpl(deploymentScenario.getNamespace())) {

            StockTickEvent stockTickEvent = new StockTickEvent("RHT",
                                                               ThreadLocalRandom.current().nextLong(80,
                                                                                                    100));
            final RemoteFactHandle<StockTickEvent> factHandle = producer.insert(stockTickEvent);

            final CompletableFuture<Long> fireAllRulesFuture = producer.fireAllRules();
            fireAllRulesFuture.get();

            final Pod leaderPod = HACEPTestsUtils.leaderPod(project);
            project.getOpenShift().deletePod(leaderPod);
            deploymentScenario.getDeployments().get(0).waitForScale();

            final CompletableFuture<StockTickEvent> factFuture = producer.getObject(factHandle);
            final StockTickEvent fact =
                    factFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT, TimeUnit.SECONDS);
            Assertions.assertThat(fact).isNotNull();
            Assertions.assertThat(fact.isProcessed()).isTrue();
        }
    }

    @Test
    public void testScaleToZeroAndBack() throws Exception {
        final TopicsConfig topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        final Properties connectionProperties = deploymentScenario.getKafkaConnectionProperties();
        connectionProperties.putAll(CommonConfig.getStatic());

        try (RemoteKieSession producer = RemoteKieSession.create(connectionProperties, topicsConfig);
             Project project = new ProjectImpl(deploymentScenario.getNamespace())) {

            StockTickEvent stockTickEvent = new StockTickEvent("RHT",
                                                               ThreadLocalRandom.current().nextLong(80,
                                                                                                    100));
            final RemoteFactHandle<StockTickEvent> factHandle = producer.insert(stockTickEvent);

            final CompletableFuture<Long> fireAllRulesFuture = producer.fireAllRules();
            fireAllRulesFuture.get();

            deploymentScenario.getDeployments()
                    .get(0)
                    .getInstances()
                    .stream()
                    .map(instance -> instance.getName())
                    .map(podName -> project.getOpenShift().getPod(podName))
                    .forEach(pod -> project.getOpenShift().deletePod(pod));
            deploymentScenario.getDeployments().get(0).waitForScale();

            CompletableFuture<Long> factCountFuture = producer.getFactCount();
            long factCount = factCountFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT,
                                                 TimeUnit.SECONDS);
            Assertions.assertThat(factCount).isEqualTo(1);

            final CompletableFuture<StockTickEvent> factFuture = producer.getObject(factHandle);
            final StockTickEvent fact =
                    factFuture.get(HACEPTestsConstants.DEFAULT_COMPLETABLE_FUTURE_TIMEOUT, TimeUnit.SECONDS);
            Assertions.assertThat(fact).isNotNull();
            Assertions.assertThat(fact.isProcessed()).isTrue();
        }
    }
}

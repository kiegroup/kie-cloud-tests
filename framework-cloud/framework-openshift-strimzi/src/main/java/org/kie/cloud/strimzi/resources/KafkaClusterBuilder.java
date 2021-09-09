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

package org.kie.cloud.strimzi.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaClusterBuilder {
    private String name;
    private int kafkaReplicas = 3;
    private Map<String, String> kafkaConfig = new HashMap<>();
    private int zookeeperReplicas = 3;
    private boolean persistent = false;

    public KafkaClusterBuilder(final String name) {
        this.name = name;
    }

    public KafkaClusterBuilder withKafkaReplicas(final int kafkaReplicas) {
        this.kafkaReplicas = kafkaReplicas;

        return this;
    }

    public KafkaClusterBuilder addKafkaConfigItem(final String key, final String value) {
        kafkaConfig.put(key, value);

        return this;
    }

    public KafkaClusterBuilder withZookeeperReplicas(final int zookeeperReplicas) {
        this.zookeeperReplicas = zookeeperReplicas;

        return this;
    }

    public KafkaClusterBuilder ephemeral() {
        this.persistent = false;

        return this;
    }

    public KafkaClusterBuilder persistent() {
        this.persistent = true;

        return this;
    }

    public KafkaCluster build() {

        final KafkaCluster kafkaCluster = new KafkaCluster();
        kafkaCluster.getMetadata().setName(name);

        final KafkaClusterSpec kafkaClusterSpec = new KafkaClusterSpec();
        kafkaClusterSpec.getKafka().put("replicas", kafkaReplicas);
        final List<Object> listeners = new ArrayList<Object>();
        final Map<String, Object> plainListener = new HashMap<>();
        plainListener.put("name", "plain");
        plainListener.put("port", 9092);
        plainListener.put("tls", false);
        plainListener.put("type", "internal");
        listeners.add(plainListener);
        final Map<String, Object> tlsListener = new HashMap<>();
        tlsListener.put("name", "tls");
        tlsListener.put("port", 9093);
        tlsListener.put("tls", true);
        tlsListener.put("type", "internal");
        listeners.add(tlsListener);
        final Map<String, Object> externalListener = new HashMap<>();
        externalListener.put("name", "external");
        externalListener.put("port", 9094);
        externalListener.put("tls", true);
        externalListener.put("type", "route");
        listeners.add(externalListener);
        kafkaClusterSpec.getKafka().put("listeners", listeners);
        kafkaClusterSpec.getKafka().put("config", kafkaConfig);
        if (persistent == true) {
            kafkaClusterSpec.getKafka().put("storage", persistentStorage());
        } else {
            kafkaClusterSpec.getKafka().put("storage", ephemeralStorage());
        }

        kafkaClusterSpec.getZookeeper().put("replicas", zookeeperReplicas);
        if (persistent == true) {
            kafkaClusterSpec.getZookeeper().put("storage", persistentStorage());
        } else {
            kafkaClusterSpec.getZookeeper().put("storage", ephemeralStorage());
        }

        kafkaClusterSpec.getEntityOperator().put("topicOperator", new HashMap<>());
        kafkaClusterSpec.getEntityOperator().put("userOperator", new HashMap<>());

        kafkaCluster.setSpec(kafkaClusterSpec);

        return kafkaCluster;
    }

    private static Map<String, Object> ephemeralStorage() {
        final Map<String, Object> storage = new HashMap<>();
        storage.put("type", "ephemeral");

        return storage;
    }

    private static Map<String, Object> persistentStorage() {
        final Map<String, Object> storage = new HashMap<>();
        storage.put("type", "persistent-claim");
        storage.put("size", "100Mi");

        return storage;
    }
}

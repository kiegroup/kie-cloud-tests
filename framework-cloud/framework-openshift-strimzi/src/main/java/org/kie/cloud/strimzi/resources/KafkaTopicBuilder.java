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

import java.util.HashMap;
import java.util.Map;

public class KafkaTopicBuilder {
    private String name;
    private String cluster;
    private int partitions = 1;
    private int replicas = 1;
    private Map<String, String> config = new HashMap<>();

    public KafkaTopicBuilder(final String name, final String cluster) {
        this.name = name;
        this.cluster = cluster;
    }

    public KafkaTopicBuilder withPartitions(final int partitions) {
        this.partitions = partitions;

        return this;
    }

    public KafkaTopicBuilder withReplicas(final int replicas) {
        this.replicas = replicas;

        return this;
    }

    public KafkaTopicBuilder addConfigItem(final String key, final String value) {
        config.put(key, value);

        return this;
    }

    public KafkaTopic build() {
        final KafkaTopic kafkaTopic = new KafkaTopic();
        kafkaTopic.getMetadata().setName(name);
        final Map<String, String> labels = new HashMap<>();
        labels.put(KafkaTopic.CLUSTER_LABEL, cluster);
        kafkaTopic.getMetadata().setLabels(labels);

        final KafkaTopicSpec kafkaTopicSpec = new KafkaTopicSpec();
        kafkaTopicSpec.setPartitions(partitions);
        kafkaTopicSpec.setReplicas(replicas);
        kafkaTopicSpec.setConfig(new HashMap<>(config));

        kafkaTopic.setSpec(kafkaTopicSpec);
        return kafkaTopic;
    }
}

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

public class KafkaClusterSpec {
    private Map<String, Object> kafka = new HashMap<>();
    private Map<String, Object> zookeeper = new HashMap<>();
    private Map<String, Object> entityOperator = new HashMap<>();

    public Map<String, Object> getKafka() {
        return kafka;
    }

    public void setKafka(Map<String, Object> kafka) {
        this.kafka = kafka;
    }

    public Map<String, Object> getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(Map<String, Object> zookeeper) {
        this.zookeeper = zookeeper;
    }

    public Map<String, Object> getEntityOperator() {
        return entityOperator;
    }

    public void setEntityOperator(Map<String, Object> entityOperator) {
        this.entityOperator = entityOperator;
    }
}

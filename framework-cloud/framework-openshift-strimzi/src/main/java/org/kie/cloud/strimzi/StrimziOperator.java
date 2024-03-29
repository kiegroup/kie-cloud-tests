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

package org.kie.cloud.strimzi;

import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.strimzi.resources.KafkaCluster;
import org.kie.cloud.strimzi.resources.KafkaTopic;

public class StrimziOperator {

    NonNamespaceOperation<KafkaCluster, KubernetesResourceList<KafkaCluster>,  Resource<KafkaCluster>> kafkaClusterClient;
    NonNamespaceOperation<KafkaTopic, KubernetesResourceList<KafkaTopic>,  Resource<KafkaTopic>> kafkaTopicClient;

    public StrimziOperator(final Project project) {
        kafkaClusterClient = OpenShifts.admin().customResources(KafkaCluster.class).inNamespace(project.getName());

        kafkaTopicClient = OpenShifts.admin().customResources(KafkaTopic.class).inNamespace(project.getName());
    }

    public void createCluster(final KafkaCluster kafkaCluster) {
        kafkaClusterClient.create(kafkaCluster);
    }

    public void createTopic(final KafkaTopic kafkaTopic) {
        kafkaTopicClient.create(kafkaTopic);
    }
}

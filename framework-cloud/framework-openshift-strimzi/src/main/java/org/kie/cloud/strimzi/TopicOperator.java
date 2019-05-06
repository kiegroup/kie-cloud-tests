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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.impl.ProjectImpl;

public class TopicOperator {
    private final String DEFAULT_TOPIC_NAME = "my-topic";
    private final String KAFKA_TOPIC_EXAMPLE = "examples/topic/kafka-topic.yaml";

    private Project project;
    private File topicYamlFile;

    public TopicOperator(final Project project, final File amqStreamsDir) {
        this.project = project;
        this.topicYamlFile = new File(amqStreamsDir, KAFKA_TOPIC_EXAMPLE);
    }

    public void createTopic(final String name) {
        String topicYaml;
        try {
            topicYaml = FileUtils.readFileToString(topicYamlFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to read topic YAML file: " + topicYamlFile.getAbsolutePath(), e);
        }
        topicYaml = topicYaml.replace(DEFAULT_TOPIC_NAME, name);

        final File tmpYamlFile;
        try {
            tmpYamlFile = File.createTempFile("topic-", ".yaml");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create temp file for topic", e);
        }

        try {
            FileUtils.writeStringToFile(tmpYamlFile, topicYaml, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Unable to write topic YAML", e);
        }

        project.createResourcesFromYamlAsAdmin(tmpYamlFile.getAbsolutePath());
    }
}

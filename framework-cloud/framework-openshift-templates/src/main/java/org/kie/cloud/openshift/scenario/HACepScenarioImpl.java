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

package org.kie.cloud.openshift.scenario;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.FileUtils;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.HACepDeployment;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.HACepDeploymentImpl;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.strimzi.TopicOperator;

public class HACepScenarioImpl extends OpenShiftScenario<HACepScenario> implements HACepScenario {

    private static final String AMQ_STREAMS_INSTALL_SUBDIRECTORY = "install/cluster-operator";
    private static final String AMQ_STREAMS_TEMPLATES_SUBDIRECTORY = "examples/templates/cluster-operator";
    private static final String KAFKA_EPHEMERAL = "examples/kafka/kafka-ephemeral.yaml";

    private static final String MASTER_EVENTS_TOPIC = "control";
    private static final String USER_INPUT_TOPIC = "events";
    private static final String SNAPSHOTS_TOPIC = "snapshot";

    private HACepDeployment haCepDeployment;

    @Override
    protected void deployKieDeployments() {
        final File amqStreamsDirectory = downloadAndUnzipAMQStreams();
        final File amqStreamsInstallDirectory = new File(amqStreamsDirectory, AMQ_STREAMS_INSTALL_SUBDIRECTORY);
        filterNamespaceInInstallationFiles(amqStreamsInstallDirectory, project.getName());

        project.createResourcesFromYamlAsAdmin(sortedFolderContent(amqStreamsInstallDirectory));
        final File amqStreamsTemplatesDirectory = new File(amqStreamsDirectory, AMQ_STREAMS_TEMPLATES_SUBDIRECTORY);

        project.createResourcesFromYamlAsAdmin(sortedFolderContent(amqStreamsTemplatesDirectory));
        final File kafkaEphemeralFile = new File(amqStreamsDirectory, KAFKA_EPHEMERAL);
        project.createResourcesFromYamlAsAdmin(kafkaEphemeralFile.getAbsolutePath());

        final TopicOperator topicOperator = new TopicOperator(project, getAMQStreamsDirectory());
        topicOperator.createTopic(MASTER_EVENTS_TOPIC);
        topicOperator.createTopic(USER_INPUT_TOPIC);
        topicOperator.createTopic(SNAPSHOTS_TOPIC);

        project.runOcCommandAsAdmin("create", "clusterrolebinding", "permissive-binding",
                                               "--clusterrole=cluster-admin", "--group=system:serviceaccounts");

        project.createResourcesFromYamlAsAdmin(OpenShiftConstants.getHaCepResourcesList());
        haCepDeployment = new HACepDeploymentImpl(project);
        project.runOcCommandAsAdmin("expose", "service",
                                    ((HACepDeploymentImpl) haCepDeployment).getServiceName());
        haCepDeployment.waitForScale();
    }

    @Override
    public void undeploy() {
        project.runOcCommandAsAdmin("delete", "clusterrolebinding", "permissive-binding");
        super.undeploy();
    }

    private File downloadAndUnzipAMQStreams() {
        File amqStreamsZipFile;
        try {
            amqStreamsZipFile = File.createTempFile("amq-streams", ".zip");
            FileUtils.copyURLToFile(new URL(OpenShiftConstants.getAMQStreamsZip()), amqStreamsZipFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to download AMQ streams zip", e);
        }

        final File amqStreamsDirectory = getAMQStreamsDirectory();
        amqStreamsDirectory.mkdir();
        final Unzip unzip = new Unzip();
        unzip.setSrc(amqStreamsZipFile);
        unzip.setDest(amqStreamsDirectory);
        unzip.execute();

        return amqStreamsDirectory;
    }

    private static void filterNamespaceInInstallationFiles(final File amqStreamsInstallDirectory,
                                                           final String projectName) {
        final List<File> installationFiles = Arrays.asList(amqStreamsInstallDirectory.listFiles());
        for (File file : installationFiles) {
            if (file.isFile() && file.getName().contains("RoleBinding")) {
                try {
                    String fileContent = FileUtils.readFileToString(file, "UTF-8");
                    fileContent = fileContent.replaceAll("namespace: .*",
                                                         "namespace: " + projectName);
                    FileUtils.write(file, fileContent, "UTF-8");
                } catch (IOException e) {
                    throw new RuntimeException("Unable to modify AMQ streams installation files");
                }
            }
        }
    }

    private static List<String> sortedFolderContent(final File folder) {
        return Arrays.asList(folder.listFiles())
                     .stream()
                     .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
                     .map(f -> f.getAbsolutePath())
                     .collect(Collectors.toList());
    }

    @Override
    protected void configureWithExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        // Nothing done
    }

    @Override
    protected void removeConfigurationFromExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        // Nothing done
    }

    @Override
    public List<Deployment> getDeployments() {
        final List<Deployment> deployments = new ArrayList<>();
        deployments.add(new HACepDeploymentImpl(project));

        return deployments;
    }

    @Override
    public File getAMQStreamsDirectory() {
        final File amqStreamsDirectory = new File(OpenShiftConstants.getAMQStreamsDir());

        return amqStreamsDirectory;
    }
}

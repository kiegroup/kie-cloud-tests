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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.openshift.api.model.Route;
import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.FileUtils;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.HACepDeployment;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.HACepDeploymentImpl;
import org.kie.cloud.strimzi.StrimziOperator;
import org.kie.cloud.strimzi.deployment.KafkaDeployment;
import org.kie.cloud.strimzi.deployment.StrimziOperatorDeployment;
import org.kie.cloud.strimzi.deployment.ZookeeperDeployment;
import org.kie.cloud.strimzi.resources.KafkaCluster;
import org.kie.cloud.strimzi.resources.KafkaClusterBuilder;

public class HACepScenarioImpl extends OpenShiftScenario<HACepScenario> implements HACepScenario {

    private static final String AMQ_STREAMS_INSTALL_SUBDIRECTORY = "install/cluster-operator";
    private static final String AMQ_STREAMS_TEMPLATES_SUBDIRECTORY = "examples/templates/cluster-operator";

    private static final String KAFKA_CLUSTER_NAME = "my-cluster";

    private static final String KAFKA_CLUSTER_CA_SECRET_SUFFIX = "-cluster-ca-cert";
    private static final String KAFKA_CLUSTER_CA_KEY = "ca.crt";
    private static final String KAFKA_CLUSTER_CA_FILE = "ca.crt";
    private static final String KAFKA_CLUSTER_KEYSTORE_FILE = "keystore.jks";
    private static final String KAFKA_CLUSTER_KEYSTORE_PASSWD = "changeit";

    private static final String KAFKA_BOOTSTRAP_ROUTE = "my-cluster-kafka-bootstrap";

    private static final String SOURCES_FILE_ROLE = "springboot/kubernetes/role.yaml";
    private static final String SOURCES_FILE_ROLE_BINDING = "springboot/kubernetes/role-binding.yaml";
    private static final String SOURCES_FILE_SERVICE_ACCOUNT = "springboot/kubernetes/service-account.yaml";

    private static final String SOURCES_KAFKA_TOPICS_FOLDER = "kafka-topics";
    /* List of topics which needs to be created for HACEP. Topics are located in kafka-topics folder in HACEP
     distribution */
    private static final String[] SOURCE_KAFKA_TOPIC_FILES =
            new String[] {"control.yaml", "events.yaml", "kiesessioninfos.yaml", "snapshot.yaml"};

    private static final String STRIMZI_LABEL_KEY = "app";
    private static final String STRIMZI_LABEL_VALUE = "strimzi";

    private StrimziOperator strimziOperator;
    private HACepDeployment haCepDeployment;

    @Override
    protected void deployKieDeployments() {
        final File amqStreamsDirectory = downloadAndUnzipAMQStreams();
        final File amqStreamsInstallDirectory = new File(amqStreamsDirectory, AMQ_STREAMS_INSTALL_SUBDIRECTORY);
        filterNamespaceInInstallationFiles(amqStreamsInstallDirectory, project.getName());

        project.createResourcesFromYamlAsAdmin(sortedFolderContent(amqStreamsInstallDirectory));
        final File amqStreamsTemplatesDirectory = new File(amqStreamsDirectory, AMQ_STREAMS_TEMPLATES_SUBDIRECTORY);

        project.createResourcesFromYamlAsAdmin(sortedFolderContent(amqStreamsTemplatesDirectory));

        final StrimziOperatorDeployment strimziOperatorDeployment = new StrimziOperatorDeployment(project);
        strimziOperatorDeployment.waitForScale();

        strimziOperator = new StrimziOperator(project);
        final KafkaClusterBuilder kafkaClusterBuilder = new KafkaClusterBuilder(KAFKA_CLUSTER_NAME)
                .addKafkaConfigItem("offsets.topic.replication.factor", "3")
                .addKafkaConfigItem("transaction.state.log.replication.factor", "3")
                .addKafkaConfigItem("transaction.state.log.min.isr", "2")
                .addKafkaConfigItem("log.message.format.version", "2.1")
                .addKafkaConfigItem("auto.create.topics.enable", "true");
        final KafkaCluster kafkaCluster = kafkaClusterBuilder.build();
        strimziOperator.createCluster(kafkaCluster);
        final ZookeeperDeployment zookeeperDeployment = new ZookeeperDeployment(kafkaCluster.getMetadata().getName(), project);
        zookeeperDeployment.waitForScale();
        final KafkaDeployment kafkaDeployment = new KafkaDeployment(kafkaCluster.getMetadata().getName(), project);
        kafkaDeployment.waitForScale();

        createTopics();

        final File haCepSourcesDir = new File(OpenShiftConstants.getHaCepSourcesDir());
        final File roleYamlFile = new File(haCepSourcesDir, SOURCES_FILE_ROLE);
        if (!roleYamlFile.isFile()) {
            throw new RuntimeException("File with HACEP role can not be found: " + roleYamlFile.getAbsolutePath());
        }
        final File serviceAccountYamlFile = new File(haCepSourcesDir, SOURCES_FILE_SERVICE_ACCOUNT);
        if (!serviceAccountYamlFile.isFile()) {
            throw new RuntimeException("File with HACEP service account can not be found: " +
                                               serviceAccountYamlFile.getAbsolutePath());
        }
        final File roleBindingYamlFile = new File(haCepSourcesDir, SOURCES_FILE_ROLE_BINDING);
        if (!serviceAccountYamlFile.isFile()) {
            throw new RuntimeException("File with HACEP role binding can not be found: " +
                                               roleBindingYamlFile.getAbsolutePath());
        }
        project.createResourcesFromYamlAsAdmin(roleYamlFile.getAbsolutePath());
        project.createResourcesFromYamlAsAdmin(serviceAccountYamlFile.getAbsolutePath());
        project.createResourcesFromYamlAsAdmin(roleBindingYamlFile.getAbsolutePath());

        project.createResourcesFromYamlAsAdmin(OpenShiftConstants.getHaCepResourcesList());
        haCepDeployment = new HACepDeploymentImpl(project);
        project.runOcCommandAsAdmin("expose", "service",
                                    ((HACepDeploymentImpl) haCepDeployment).getServiceName());
        haCepDeployment.waitForScale();
        try {
            Thread.sleep(30000); // RHPAM-2380
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void undeploy() {
        super.undeploy();

        deleteStrimziCustomResourceDefinitions();
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

    private void createTopics() {
        final File kafkaTopicsFolder = new File(OpenShiftConstants.getHaCepSourcesDir(), SOURCES_KAFKA_TOPICS_FOLDER);
        for (final String topicFileName : SOURCE_KAFKA_TOPIC_FILES) {
            final File kafkaTopicFile = new File(kafkaTopicsFolder, topicFileName);
            project.createResourcesFromYamlAsAdmin(kafkaTopicFile.getAbsolutePath());
        }
    }

    private void deleteStrimziCustomResourceDefinitions() {
        project.runOcCommandAsAdmin("delete", "customresourcedefinition", "-l", STRIMZI_LABEL_KEY
                                            + "=" + STRIMZI_LABEL_VALUE);
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

    @Override
    public File getKafkaKeyStore() {
        final File kafkaCertificateFile = getKafkaCertificate();
        final File kafkaKSFile = new File(OpenShiftConstants.getProjectBuildDirectory(), KAFKA_CLUSTER_KEYSTORE_FILE);

        X509Certificate certificate;
        try (InputStream is = new FileInputStream(kafkaCertificateFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            certificate = (X509Certificate) cf.generateCertificate(is);
            certificate.verify(certificate.getPublicKey());
        } catch (Exception e) {
            throw new RuntimeException("Unable to read file with Kafka key", e);
        }

        try (final OutputStream kafkaKSOutputStream = new FileOutputStream(kafkaKSFile)) {
            final KeyStore kafkaKS = KeyStore.getInstance("JKS");
            kafkaKS.load(null, KAFKA_CLUSTER_KEYSTORE_PASSWD.toCharArray());
            kafkaKS.setCertificateEntry("test", certificate);
            kafkaKS.store(kafkaKSOutputStream, KAFKA_CLUSTER_KEYSTORE_PASSWD.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Unable to create key store", e);
        }

        return kafkaKSFile;
    }

    @Override
    public Properties getKafkaConnectionProperties() {
        final Route kafkaBootstrapRoute = project.getOpenShift().getRoute(KAFKA_BOOTSTRAP_ROUTE);
        final String kafkaBootstrapHost = kafkaBootstrapRoute.getSpec().getHost();
        final File keyStore = getKafkaKeyStore();

        final Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaBootstrapHost + ":443");
        properties.put("security.protocol", "SSL");
        properties.put("ssl.keystore.location", keyStore.getAbsolutePath());
        properties.put("ssl.keystore.password", KAFKA_CLUSTER_KEYSTORE_PASSWD);
        properties.put("ssl.truststore.location", keyStore.getAbsolutePath());
        properties.put("ssl.truststore.password", KAFKA_CLUSTER_KEYSTORE_PASSWD);

        return properties;
    }

    private File getKafkaCertificate() {
        final File certificateFile = new File(OpenShiftConstants.getProjectBuildDirectory(), KAFKA_CLUSTER_CA_FILE);

        final Secret secret = project.getOpenShift()
                .getSecret(KAFKA_CLUSTER_NAME + KAFKA_CLUSTER_CA_SECRET_SUFFIX);
        final Map<String, String> data = secret.getData();
        final String certificate = data.get(KAFKA_CLUSTER_CA_KEY);
        try {
            FileUtils.writeByteArrayToFile(certificateFile, Base64.getDecoder().decode(certificate));
        } catch (IOException e) {
            throw new RuntimeException("Error writing file with Kafka SSL certificate", e);
        }

        return certificateFile;
    }
}

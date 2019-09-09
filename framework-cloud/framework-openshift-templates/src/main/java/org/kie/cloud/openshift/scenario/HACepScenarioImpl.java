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
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String SOURCES_FILE_HACEP_DEPLOYMENT = "springboot/kubernetes/deployment.yaml";
    private static final String SOURCES_FILE_HACEP_SERVICE = "springboot/kubernetes/service.yaml";

    private static final String SOURCES_KAFKA_TOPICS_FOLDER = "kafka-topics";
    /* List of topics which needs to be created for HACEP. Topics are located in kafka-topics folder in HACEP
     distribution */
    private static final String[] SOURCE_KAFKA_TOPIC_FILES =
            new String[] {"control.yaml", "events.yaml", "kiesessioninfos.yaml", "snapshot.yaml"};

    private static final String STRIMZI_LABEL_KEY = "app";
    private static final String STRIMZI_LABEL_VALUE = "strimzi";

    private StrimziOperator strimziOperator;
    private HACepDeployment haCepDeployment;

    private static final Logger logger = LoggerFactory.getLogger(HACepScenarioImpl.class);

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
        logger.info("Deploying Kafka cluster");
        strimziOperator.createCluster(kafkaCluster);
        final ZookeeperDeployment zookeeperDeployment = new ZookeeperDeployment(kafkaCluster.getMetadata().getName(), project);
        zookeeperDeployment.waitForScale();
        final KafkaDeployment kafkaDeployment = new KafkaDeployment(kafkaCluster.getMetadata().getName(), project);
        kafkaDeployment.waitForScale();

        createTopics();

        final File haCepSourcesDir = new File(OpenShiftConstants.getHaCepSourcesDir());
        final File roleYamlFile = new File(haCepSourcesDir, SOURCES_FILE_ROLE);
        if (!roleYamlFile.isFile()) {
            logger.error("File with HACEP role can not be found: {}", roleYamlFile.getAbsolutePath());
            throw new RuntimeException("File with HACEP role can not be found: " + roleYamlFile.getAbsolutePath());
        }
        final File serviceAccountYamlFile = new File(haCepSourcesDir, SOURCES_FILE_SERVICE_ACCOUNT);
        if (!serviceAccountYamlFile.isFile()) {
            logger.error("File with HACEP service account can not be found: {}",
                         serviceAccountYamlFile.getAbsolutePath());
            throw new RuntimeException("File with HACEP service account can not be found: " +
                                               serviceAccountYamlFile.getAbsolutePath());
        }
        final File roleBindingYamlFile = new File(haCepSourcesDir, SOURCES_FILE_ROLE_BINDING);
        if (!serviceAccountYamlFile.isFile()) {
            throw new RuntimeException("File with HACEP role binding can not be found: " +
                                               roleBindingYamlFile.getAbsolutePath());
        }
        logger.info("Creating role for HACEP from file: {}", roleYamlFile.getAbsolutePath());
        project.createResourcesFromYamlAsAdmin(roleYamlFile.getAbsolutePath());
        logger.info("Creating service account for HACEP from file: {}", serviceAccountYamlFile.getAbsolutePath());
        project.createResourcesFromYamlAsAdmin(serviceAccountYamlFile.getAbsolutePath());
        logger.info("Creating role binding for HACEP from file: {}", roleBindingYamlFile.getAbsolutePath());
        project.createResourcesFromYamlAsAdmin(roleBindingYamlFile.getAbsolutePath());

        final String dockerImageRepository = buildHACEPImage();
        final File haCepDeploymentYamlFile = new File(haCepSourcesDir, SOURCES_FILE_HACEP_DEPLOYMENT);
        final String haCepDeploymentYaml = patchHACEPDeploymentFile(haCepDeploymentYamlFile, dockerImageRepository);
        logger.info("Creating HACEP deployment");
        project.createResourcesFromYamlStringAsAdmin(haCepDeploymentYaml);

        final File haCepService = new File(haCepSourcesDir, SOURCES_FILE_HACEP_SERVICE);
        logger.info("Creating HACEP service from file: {}", haCepService.getAbsolutePath());
        project.createResourcesFromYamlAsAdmin(haCepService.getAbsolutePath());

        haCepDeployment = new HACepDeploymentImpl(project);

        logger.info("Exposing HACEP service as route");
        project.runOcCommandAsAdmin("expose", "service",
                                    ((HACepDeploymentImpl) haCepDeployment).getServiceName());
        haCepDeployment.waitForScale();
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
            logger.info("Downloading zip with AMQ Streams from {} to {}", OpenShiftConstants.getAMQStreamsZip(),
                        amqStreamsZipFile.getAbsolutePath());
            FileUtils.copyURLToFile(new URL(OpenShiftConstants.getAMQStreamsZip()), amqStreamsZipFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to download AMQ streams zip", e);
        }

        final File amqStreamsDirectory = getAMQStreamsDirectory();
        amqStreamsDirectory.mkdir();
        final Unzip unzip = new Unzip();
        unzip.setSrc(amqStreamsZipFile);
        unzip.setDest(amqStreamsDirectory);
        logger.info("Unpacking AMQ streams zip from {} to {}", amqStreamsZipFile.getAbsolutePath(),
                    amqStreamsDirectory.getAbsolutePath());
        unzip.execute();

        return amqStreamsDirectory;
    }

    private String buildHACEPImage() {
        project.runOcCommandAsAdmin("new-build", "--binary", "--strategy=docker",
                                    "--name", "openshift-kie-springboot");
        final File springModuleDir = new File(OpenShiftConstants.getHaCepSourcesDir(), "springboot");
        logger.info("Building HA-CEP Spring boot image");
        final String buildOutput = project.runOcCommandAsAdmin("start-build", "openshift-kie-springboot",
                                    "--from-dir=" + springModuleDir.getAbsolutePath(), "--follow");
        logger.info(buildOutput);
        final String dockerImageRepository = project.getOpenShiftAdmin().getImageStream("openshift-kie-springboot").getStatus().getDockerImageRepository();

        return dockerImageRepository;
    }

    private String patchHACEPDeploymentFile(final File deploymentYamlFile, final String dockerImageRepository) {
        String deploymentYaml;
        try {
            deploymentYaml = FileUtils.readFileToString(deploymentYamlFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Error loading yaml file with HACEP deployment" +
                                               deploymentYamlFile.getAbsolutePath(), e);
        }

        logger.info("Changing image name in HACEP deployment from file: {}", deploymentYamlFile.getAbsolutePath());
        deploymentYaml = deploymentYaml.replaceAll("image:.*", "image: " + dockerImageRepository);

        return deploymentYaml;
    }

    private void createTopics() {
        final File kafkaTopicsFolder = new File(OpenShiftConstants.getHaCepSourcesDir(), SOURCES_KAFKA_TOPICS_FOLDER);
        for (final String topicFileName : SOURCE_KAFKA_TOPIC_FILES) {
            final File kafkaTopicFile = new File(kafkaTopicsFolder, topicFileName);
            logger.info("Creating topic from file: {}", kafkaTopicFile.getAbsolutePath());
            project.createResourcesFromYamlAsAdmin(kafkaTopicFile.getAbsolutePath());
        }
    }

    private void deleteStrimziCustomResourceDefinitions() {
        logger.info("Deleting AMQ streams custom resource definitions");
        project.runOcCommandAsAdmin("delete", "customresourcedefinition", "-l", STRIMZI_LABEL_KEY
                                            + "=" + STRIMZI_LABEL_VALUE);
    }

    private static void filterNamespaceInInstallationFiles(final File amqStreamsInstallDirectory,
                                                           final String projectName) {
        final List<File> installationFiles = Arrays.asList(amqStreamsInstallDirectory.listFiles());
        for (File file : installationFiles) {
            if (file.isFile() && file.getName().contains("RoleBinding")) {
                try {
                    logger.info("Changing namespace name in file: {}", file.getAbsolutePath());
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

    public Map<String, String> getScenarioEnvironment() {
        return new HashMap<>();
    }
}

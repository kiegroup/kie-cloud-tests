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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.openshift.api.model.Route;
import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.HACepDeployment;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.api.scenario.HACepScenario;
import org.kie.cloud.api.scenario.builder.WorkbenchKieServerScenarioBuilder;
import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.HACepDeploymentImpl;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
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

    private static final String HACEP_CONTAINER_NAME = "openshift-kie-springboot";

    private static final String IMAGE_BUILD_ARTIFACT_NAME = "openshift-kie-springboot";
    private static final String USER_ID_DOCKERFILE_PLACEHOLDER = "<id_user>";
    private static final String GROUP_ID_DOCKERFILE_PLACEHOLDER = "<id_group>";

    private Map<String, String> springDeploymentEnvironmentVariables = new HashMap<>();

    private StrimziOperator strimziOperator;
    private HACepDeployment haCepDeployment;
    private List<String> kjars = new ArrayList<>();

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

        logger.info("Building and deploying kjars");
        buildAndDeployKjars();

        final String userUUID = project.getOpenShift().getProject(projectName)
                .getMetadata().getAnnotations().get("openshift.io/sa.scc.uid-range").split("/")[0];

        final String dockerImageRepository = buildHACEPImage(userUUID);
        final File haCepDeploymentYamlFile = new File(haCepSourcesDir, SOURCES_FILE_HACEP_DEPLOYMENT);

        deployHACEPDeployment(haCepDeploymentYamlFile, dockerImageRepository, userUUID, springDeploymentEnvironmentVariables);

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
        final File logsDirectory = logsDirectory();
        final List<Instance> instances = haCepDeployment.getInstances();
        for (Instance instance : instances) {
            File logFile = new File(logsDirectory, instance.getName() + ".log");
            String logText = instance.getLogs();
            try {
                FileUtils.writeStringToFile(logFile, logText, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException("Error writing logs for Openshift pod", e);
            }
        }

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

    private String buildHACEPImage(final String userUUID) {
        final File springModuleDir = new File(OpenShiftConstants.getHaCepSourcesDir(), "springboot");
        final File dockerFile = new File(springModuleDir, "Dockerfile");
        try {
            final String originalDockerFileContent = FileUtils.readFileToString(dockerFile, "UTF-8");
            String dockerFileContent = originalDockerFileContent.replace(USER_ID_DOCKERFILE_PLACEHOLDER, userUUID);
            dockerFileContent = dockerFileContent.replace(GROUP_ID_DOCKERFILE_PLACEHOLDER, "1000");
            FileUtils.writeStringToFile(dockerFile, dockerFileContent, "UTF-8");

            project.runOcCommandAsAdmin("new-build", "--binary", "--strategy=docker",
                                        "--name", IMAGE_BUILD_ARTIFACT_NAME);
            logger.info("Building HA-CEP Spring boot image");
            final String buildOutput = project.runOcCommandAsAdmin("start-build", IMAGE_BUILD_ARTIFACT_NAME,
                                                                   "--from-dir=" + springModuleDir.getAbsolutePath(), "--follow");
            logger.info(buildOutput);

            FileUtils.writeStringToFile(dockerFile, originalDockerFileContent, "UTF-8");
        } catch (IOException e) {
            logger.error("Unable to read/write Dockerfile {}", dockerFile);
            throw new RuntimeException("Unable to read/write Dockerfile", e);
        }

        final String dockerImageRepository = project.getOpenShiftAdmin().getImageStream(IMAGE_BUILD_ARTIFACT_NAME)
                .getStatus().getDockerImageRepository();

        return dockerImageRepository;
    }

    private void deployHACEPDeployment(
            final File deploymentYamlFile,
            final String dockerImageRepository,
            final String userUUID,
            final Map<String, String> additionalEnvVars) {
        try {
            String deploymentYamlContent = FileUtils.readFileToString(deploymentYamlFile, "UTF-8");
            deploymentYamlContent = deploymentYamlContent.replace(USER_ID_DOCKERFILE_PLACEHOLDER, "0");
            FileUtils.writeStringToFile(deploymentYamlFile, deploymentYamlContent);

            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            io.fabric8.kubernetes.api.model.apps.Deployment deployment =
                    objectMapper.readValue(deploymentYamlFile, io.fabric8.kubernetes.api.model.apps.Deployment.class);
            final Optional<Container> container = deployment.getSpec().getTemplate().getSpec().getContainers()
                    .stream()
                    .filter(c -> c.getName().equals(HACEP_CONTAINER_NAME))
                    .findFirst();
            if (!container.isPresent()) {
                logger.error("Can not find HA-CEP container in deployment");
                throw new RuntimeException("Can not find HA-CEP container in deployment");
            }
            container.get().getSecurityContext().setRunAsUser(Long.valueOf(userUUID));
            container.get().setImage(dockerImageRepository);
            if (container.get().getEnv() == null) {
                container.get().setEnv(new ArrayList<>());
            }

            if (externalDeployments.stream()
                    .anyMatch(d -> ExternalDeployment.ExternalDeploymentID.MAVEN_REPOSITORY.equals(d.getKey()))) {
                final MavenRepositoryDeployment mavenRepositoryDeployment = this.getMavenRepositoryDeployment();

                container.get().getEnv().add(new EnvVar("MAVEN_LOCAL_REPO", "/app/.m2/repository", null));
                container.get().getEnv().add(new EnvVar("MAVEN_MIRROR_URL", mavenRepositoryDeployment.getSnapshotsRepositoryUrl().toString(), null));
                container.get().getEnv().add(new EnvVar("MAVEN_SETTINGS_XML", "/app/.m2/settings.xml", null));
            }
            for (final Map.Entry<String, String> envVariable : additionalEnvVars.entrySet()) {
                final EnvVar envVar= new EnvVar(envVariable.getKey(), envVariable.getValue(), null);
                container.get().getEnv().add(envVar);
            }

            project.getOpenShiftAdmin().apps().deployments().create(deployment);
        } catch (Exception e) {
            logger.error("Can not create Spring app deployment: {}", e);
            throw new RuntimeException("Can not create Spring app deployment", e);
        }
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

    private File logsDirectory() {
        final File current = new File(OpenShiftConstants.getProjectBuildDirectory());
        final File dir = new File(current, "/pod-logs");

        if (dir.isDirectory() == false) {
            dir.mkdir();
        }

        return dir;
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
    public void setKjars(List<String> kjars) {
        this.kjars = new ArrayList<>(kjars);
    }

    @Override
    public void setSpringDeploymentEnvironmentVariables(Map<String, String> springDeploymentEnvironmentVariables) {
        this.springDeploymentEnvironmentVariables = springDeploymentEnvironmentVariables;
    }

    private void buildAndDeployKjars() {
        if (!kjars.isEmpty()) {
            final MavenRepositoryDeployment mavenRepositoryDeployment = this.getMavenRepositoryDeployment();

            for (String kjar : kjars) {
                logger.info("Building and deploying kjar: {}", kjar);
                MavenDeployer.buildAndDeployMavenProject(HACepScenarioImpl.class.getResource(kjar)
                                                                 .getFile(), mavenRepositoryDeployment);
            }
        }
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

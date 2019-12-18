/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.PrometheusDeployment;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.PrometheusDeploymentImpl;
import org.kie.cloud.openshift.prometheus.servicemonitor.ServiceMonitor;
import org.kie.cloud.openshift.prometheus.servicemonitor.ServiceMonitorDoneable;
import org.kie.cloud.openshift.prometheus.servicemonitor.ServiceMonitorList;
import org.kie.cloud.openshift.prometheus.servicemonitor.components.AuthOption;
import org.kie.cloud.openshift.prometheus.servicemonitor.components.BasicAuth;
import org.kie.cloud.openshift.prometheus.servicemonitor.components.Endpoint;
import org.kie.cloud.openshift.prometheus.servicemonitor.components.Selector;
import org.kie.cloud.openshift.prometheus.servicemonitor.components.Spec;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.operator.OperatorDeployer;
import org.kie.cloud.openshift.util.operator.OperatorSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for deploying Prometheus monitoring to OpenShift project.
 */
public class PrometheusDeployer {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final Logger logger = LoggerFactory.getLogger(PrometheusDeployer.class);

    private static final String PROMETHEUS_OPERATOR_SERVICE_ACCOUNT = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-service-account.yaml";
    private static final String PROMETHEUS_SERVICE_ACCOUNT = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus/prometheus-service-account.yaml";
    private static final String PROMETHEUS_OPERATOR_CLUSTER_ROLE = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role.yaml";
    private static final String PROMETHEUS_CLUSTER_ROLE = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus/prometheus-cluster-role.yaml";
    private static final String PROMETHEUS_OPERATOR_CLUSTER_ROLE_BINDING = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role-binding.yaml";
    private static final String PROMETHEUS_CLUSTER_ROLE_BINDING = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus/prometheus-cluster-role-binding.yaml";
    private static final String PROMETHEUS_OPERATOR_DEPLOYMENT = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-deployment.yaml";
    private static final String PROMETHEUS_CUSTOM_RESOURCE = "https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus/prometheus.yaml";

    private static final String METRIC_SECRET_NAME = "metrics-secret";
    private static final String METRIC_SECRET_USERNAME_KEY = "username";
    private static final String METRIC_SECRET_PASSWORD_KEY = "password";

    private static final String PROMETHEUS_OPERATOR_NAME = "prometheus";

    public static PrometheusDeployment deploy(Project project, KieServerDeployment kieServerDeployment) {
        addClusterRoleToAdminUser(project);

        createServiceAccount(project, PROMETHEUS_OPERATOR_SERVICE_ACCOUNT);
        createServiceAccount(project, PROMETHEUS_SERVICE_ACCOUNT);
        addPrometheusOperatorSecurityConstrains(project);

        createPrometheusOperatorClusterRole(project, PROMETHEUS_OPERATOR_CLUSTER_ROLE);
        createPrometheusOperatorClusterRoleBinding(project, PROMETHEUS_OPERATOR_CLUSTER_ROLE_BINDING);
        createPrometheusOperatorDeployment(project, PROMETHEUS_OPERATOR_DEPLOYMENT);

        createPrometheusOperatorClusterRole(project, PROMETHEUS_CLUSTER_ROLE);
        createPrometheusOperatorClusterRoleBinding(project, PROMETHEUS_CLUSTER_ROLE_BINDING);
        createPrometheusCustomResource(project, PROMETHEUS_CUSTOM_RESOURCE);
        exposePrometheusRoute(project);

        createMetricsSecret(project, kieServerDeployment);
        createServiceMonitorCustomResource(project);

        PrometheusDeployment prometheusDeployment = new PrometheusDeploymentImpl(project);
        return prometheusDeployment;
    }

    public static PrometheusDeployment deployAsOperator(Project project, KieServerDeployment kieServerDeployment) {
        OperatorDeployer.deploy(project, PROMETHEUS_OPERATOR_NAME, "beta", OperatorSource.COMMUNITY);

        createServiceAccount(project, PROMETHEUS_SERVICE_ACCOUNT);
        createPrometheusOperatorClusterRole(project, PROMETHEUS_CLUSTER_ROLE);
        createPrometheusOperatorClusterRoleBinding(project, PROMETHEUS_CLUSTER_ROLE_BINDING);
        createPrometheusCustomResource(project, PROMETHEUS_CUSTOM_RESOURCE);
        exposePrometheusRoute(project);

        createMetricsSecret(project, kieServerDeployment);
        createServiceMonitorCustomResource(project);

        PrometheusDeployment prometheusDeployment = new PrometheusDeploymentImpl(project);
        return prometheusDeployment;
    }

    public static void undeployOperator(Project project) {
        OperatorDeployer.undeploy(project, PROMETHEUS_OPERATOR_NAME);
    }

    private static void addClusterRoleToAdminUser(Project project) {
        String execute = project.runOcCommandAsAdmin("adm", "policy", "add-cluster-role-to-user", "cluster-admin", OpenShiftConstants.getOpenShiftAdminUserName());
        logger.info(execute);
    }

    private static void createServiceAccount(Project project, String serviceAccountUrl) {
        try {
            ServiceAccount serviceAccount = project.getOpenShift().serviceAccounts().load(new URL(serviceAccountUrl)).get();
            serviceAccount.getMetadata().setNamespace(project.getName());
            project.getOpenShift().serviceAccounts().create(serviceAccount);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed service account URL: " + serviceAccountUrl, e);
        }
    }

    private static void createPrometheusOperatorClusterRole(Project project, String clusterRoleUrl) {
        String execute = project.runOcCommandAsAdmin("apply", "-f", clusterRoleUrl);
        logger.info(execute);
    }

    private static void createPrometheusOperatorClusterRoleBinding(Project project, String clusterRoleBindingUrl) {
        try {
            ClusterRoleBinding clusterRoleBinding = project.getOpenShift().rbac().clusterRoleBindings().load(new URL(clusterRoleBindingUrl)).get();
            clusterRoleBinding.getSubjects().get(0).setNamespace(project.getName());
            clusterRoleBinding.getMetadata().setNamespace(project.getName());
            clusterRoleBinding.setApiVersion("rbac.authorization.k8s.io/v1"); // Workaround to make REST call work
            OpenShifts.admin().inNamespace(project.getName()).rbac().clusterRoleBindings().createOrReplace(clusterRoleBinding);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed cluster role binding URL: " + clusterRoleBindingUrl, e);
        }
    }

    private static void addPrometheusOperatorSecurityConstrains(Project project) {
        String execute = project.runOcCommandAsAdmin("adm", "policy", "add-scc-to-user", "privileged", "-n", project.getName(), "-z", "prometheus-operator");
        logger.info(execute);
        execute = project.runOcCommandAsAdmin("adm", "policy", "add-scc-to-user", "privileged", "-n", project.getName(), "-z", "prometheus");
        logger.info(execute);
    }

    private static void createPrometheusOperatorDeployment(Project project, String deploymentUrl) {
        try {
            Deployment deployment = project.getOpenShift().apps().deployments().load(new URL(deploymentUrl)).get();
            deployment.getMetadata().setNamespace(project.getName());
            Path deploymentFile = storeObjectAsYamlToTempFile(deployment);
            String execute = project.runOcCommandAsAdmin("apply", "-f", deploymentFile.toString());
            logger.info(execute);

            project.getOpenShift().waiters().areExactlyNPodsReady(1, "app.kubernetes.io/name", "prometheus-operator").waitFor();
            project.getOpenShift().waiters().areExactlyNPodsRunning(1, "app.kubernetes.io/name", "prometheus-operator").waitFor();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed deployment URL: " + deploymentUrl, e);
        }
    }

    private static void createPrometheusCustomResource(Project project, String customResourceUrl) {
        try (InputStream in = (new URL(customResourceUrl)).openStream(); BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String customResource = br.lines().collect(Collectors.joining(System.lineSeparator()));
            Path customResourceFile = storeStringAsYamlToTempFile(customResource);
            String execute = project.runOcCommandAsAdmin("apply", "-f", customResourceFile.toString());
            logger.info(execute);

            project.getOpenShift().waiters().areExactlyNPodsReady(2, "prometheus", "prometheus").waitFor();
            project.getOpenShift().waiters().areExactlyNPodsRunning(2, "prometheus", "prometheus").waitFor();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed deployment URL: " + customResourceUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("IO exception while loading custom resource.", e);
        }
    }

    private static void createMetricsSecret(Project project, KieServerDeployment kieServerDeployment) {
        SecretBuilder secretBuilder = new SecretBuilder(METRIC_SECRET_NAME);
        secretBuilder.addRawData(METRIC_SECRET_USERNAME_KEY, kieServerDeployment.getUsername());
        secretBuilder.addRawData(METRIC_SECRET_PASSWORD_KEY, kieServerDeployment.getPassword());
        Secret metricSecret = secretBuilder.build();
        project.getOpenShift().createSecret(metricSecret);
    }

    private static void createServiceMonitorCustomResource(Project project) {
        CustomResourceDefinition customResourceDefinition = OpenShifts.admin().customResourceDefinitions().withName("servicemonitors.monitoring.coreos.com").get();
        NonNamespaceOperation<ServiceMonitor, ServiceMonitorList, ServiceMonitorDoneable, Resource<ServiceMonitor, ServiceMonitorDoneable>> serviceMonitorClient = OpenShifts.admin().customResources(customResourceDefinition, ServiceMonitor.class, ServiceMonitorList.class, ServiceMonitorDoneable.class).inNamespace(project.getName());
       
        AuthOption username = new AuthOption();
        username.setName(METRIC_SECRET_NAME);
        username.setKey(METRIC_SECRET_USERNAME_KEY);
        AuthOption password = new AuthOption();
        password.setName(METRIC_SECRET_NAME);
        password.setKey(METRIC_SECRET_PASSWORD_KEY);

        BasicAuth basicAuth = new BasicAuth();
        basicAuth.setUsername(username);
        basicAuth.setPassword(password);

        Endpoint endpoint = new Endpoint();
        endpoint.setTargetPort(8080);
        endpoint.setPath("/services/rest/metrics");
        endpoint.setBasicAuth(basicAuth);

        Selector selector = new Selector();

        String serviceName = project.getOpenShift().getServices().stream()
                .map(s -> s.getMetadata().getName())
                .filter(n -> n.contains("kieserver"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Kie Server service was not found"));

        selector.addMatchLabel("service", project.getOpenShift().getService(serviceName)
                                                                .getMetadata()
                                                                .getLabels()
                                                                .get("service"));

        Spec spec = new Spec();
        spec.setSelector(selector);
        spec.addEndpoint(endpoint);

        ServiceMonitor serviceMonitor = new ServiceMonitor();
        serviceMonitor.getMetadata().setName("example-app");
        serviceMonitor.getMetadata().setLabels(Collections.singletonMap("team", "frontend"));
        serviceMonitor.setSpec(spec);

        serviceMonitorClient.create(serviceMonitor);
    }

    private static void exposePrometheusRoute(Project project) {
        String execute = project.runOcCommandAsAdmin("expose", "service", "prometheus-operated");
        logger.info(execute);
    }

    private static Path storeObjectAsYamlToTempFile(Object objectToBeMapped) {
        try {
            byte[] yamlValue = YAML_MAPPER.writeValueAsBytes(objectToBeMapped);
            return Files.write(File.createTempFile("prometheus", ".yaml").toPath(), yamlValue);
        } catch (IOException e) {
            throw new RuntimeException("Error while storing object to temporary YAML file.", e);
        }
    }

    private static Path storeStringAsYamlToTempFile(String stringToBeStored) {
        try {
            return Files.write(File.createTempFile("prometheus", ".yaml").toPath(), stringToBeStored.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while storing object to temporary YAML file.", e);
        }
    }
}

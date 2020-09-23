/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.resource.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cz.xtf.builder.builders.ImageStreamBuilder;
import cz.xtf.builder.builders.ImageStreamBuilder.TagReferencePolicyType;
import cz.xtf.builder.builders.SecretBuilder;
import cz.xtf.core.http.HttpsException;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.ImageStream;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.OpenshiftInstanceUtil;
import org.kie.cloud.openshift.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public class ProjectImpl implements Project {

    private static final Logger logger = LoggerFactory.getLogger(ProjectImpl.class);

    public static final String POD_STATUS_PENDING = "Pending";

    private String projectName;
    private OpenShift openShift;
    private OpenShift openShiftAdmin;

    public ProjectImpl(String projectName) {
        this.projectName = projectName;
        this.openShift = OpenShiftController.getOpenShift(projectName);
        this.openShiftAdmin = OpenShiftController.getOpenShiftAdmin(projectName);
    }

    @Override
    public String getName() {
        return projectName;
    }

    @Override
    public OpenShift getOpenShift() {
        return openShift;
    }

    @Override
    public OpenShift getOpenShiftAdmin() {
        return openShiftAdmin;
    }

    @Override
    public void delete() {
        openShift.deleteProject();
    }

    @Override
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables) {
        boolean templateIsFile = templateUrl.getProtocol().equals("file");

        // Used to log into OpenShift
        OpenShiftBinary oc = getOpenShiftBinary(getName());

        List<String> commandParameters = new ArrayList<>();
        commandParameters.add(getOpenShiftBinaryPath());
        commandParameters.add("process");
        commandParameters.add("-f");
        commandParameters.add(templateIsFile ? templateUrl.getPath() : templateUrl.toExternalForm());
        commandParameters.add("--local");
        commandParameters.add("--ignore-unknown-parameters=true");
        commandParameters.add("-o");
        commandParameters.add("yaml");
        for (Entry<String, String> envVariable : envVariables.entrySet()) {
            commandParameters.add("-p");
            commandParameters.add(envVariable.getKey() + "=" + envVariable.getValue());
        }
        String completeProcessingCommand = commandParameters.stream().collect(Collectors.joining(" "));

        try (ProcessExecutor executor = new ProcessExecutor()) {
            File processedTemplate = executor.executeProcessCommandToTempFile(completeProcessingCommand);
            oc.execute("create", "-n", getName(), "-f", processedTemplate.getAbsolutePath());
        }
        // TODO: Temporary workaround to wait until scenario is completely initialized as there is a delay between finishing template creation command
        // and actual creation of resources on OpenShift. This should be removed when deployments won't be scaled in the beginning and will contain availability check.
        try {
            TimeUnit.SECONDS.sleep(10L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for scenario to be initialized.", e);
        }
    }

    @Override
    public void createSecret(String secretName, Map<String, String> secrets) {
        SecretBuilder builder = new SecretBuilder(secretName);
        for (Entry<String, String> entry : secrets.entrySet()) {
            builder.addRawData(entry.getKey(), entry.getValue());
        }

        openShift.createSecret(builder.build());
    }

    @Override
    public void createResources(String resourceUrl) {
        try {
            KubernetesList resourceList = openShift.lists().inNamespace(projectName).load(new URL(resourceUrl)).get();
            openShift.lists().inNamespace(projectName).create(resourceList);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        }
    }

    @Override
    public void createResourcesFromYaml(String yamlUrl) {
        final String output = openShiftBinaryClient().execute("create", "-f", yamlUrl);
        logger.info("Yaml resources from file {} were created by oc client. Output = {}", yamlUrl, output);
    }

    @Override
    public void createResourcesFromYaml(List<String> yamlUrls) {
        final OpenShiftBinary oc = openShiftBinaryClient();
        for (String url : yamlUrls) {
            final String output = oc.execute("create", "-f", url);
            logger.info("Yaml resources from file {} were created by oc client. Output = {}", url, output);
        }
    }

    @Override
    public void createResourceFromYamlString(String yamlString) {
        try {
            final File tmpYamlFile = File.createTempFile("openshift-resource-",".yaml");
            Files.write(tmpYamlFile.toPath(), yamlString.getBytes("UTF-8"));
            final String output = openShiftBinaryClient().execute("create", "-f", tmpYamlFile.getAbsolutePath());
            logger.info("Yaml resources from string was created by oc client. Output = {}", output);
        } catch (IOException e) {
            throw new RuntimeException("Error creating resource from string", e);
        }
    }

    @Override
    public void createResourcesFromYamlAsAdmin(String yamlUrl) {
        final String output = openShiftBinaryClientAsAdmin().execute("create", "-f", yamlUrl);
        logger.info("Yaml resources from file {} were created by oc client. Output = {}", yamlUrl, output);
    }

    @Override
    public void createResourcesFromYamlAsAdmin(List<String> yamlUrls) {
        final OpenShiftBinary oc = openShiftBinaryClientAsAdmin();
        for (String url : yamlUrls) {
            final String output = oc.execute("create", "-f", url);
            logger.info("Yaml resources from file {} were created by oc client. Output = {}", url, output);
        }
    }

    @Override
    public void createResourcesFromYamlStringAsAdmin(String yamlString) {
        try {
            final File tmpYamlFile = File.createTempFile("openshift-resource-",".yaml");
            Files.write(tmpYamlFile.toPath(), yamlString.getBytes("UTF-8"));
            final String output = openShiftBinaryClientAsAdmin().execute("create", "-f", tmpYamlFile.getAbsolutePath());
            logger.info("Yaml resources from string was created by oc client. Output = {}", output);
        } catch (IOException e) {
            throw new RuntimeException("Error creating resource from string", e);
        }
    }

    private OpenShiftBinary openShiftBinaryClient() {
        Optional<OpenShiftBinary> oc;
        synchronized (ProjectImpl.class) {
            oc = Optional.of(OpenShifts.masterBinary(this.getName()));
            oc.get().login(OpenShiftConstants.getOpenShiftUrl(), OpenShiftConstants.getOpenShiftUserName(),
                           OpenShiftConstants.getOpenShiftPassword());
        }
        return oc.orElseThrow(RuntimeException::new);
    }

    private OpenShiftBinary openShiftBinaryClientAsAdmin() {
        Optional<OpenShiftBinary> oc = Optional.empty();
        synchronized (ProjectImpl.class) {
            logger.debug("Try to get master binary few times as OpenShifts sometimes throws Socket exception for Connection reset");
            for (int attempt = 0; attempt < 5; attempt++) {
                try {
                    oc = Optional.of(OpenShifts.masterBinary(this.getName()));
                    oc.get().login(OpenShiftConstants.getOpenShiftUrl(), OpenShiftConstants.getOpenShiftAdminUserName(),
                                   OpenShiftConstants.getOpenShiftAdminPassword());
                    break;
                } catch (HttpsException ex) {
                    logger.warn("Was caught exception from OpenShifts on " + attempt
                            + " attempt. Trying to get master binaries again.", ex);
                }
            }
        }

        return oc.orElseThrow(RuntimeException::new);
    }

    @Override
    public void createResources(InputStream inputStream) {
        KubernetesList resourceList = openShift.lists().inNamespace(projectName).load(inputStream).get();
        openShift.lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void createImageStream(String imageStreamName, String imageTag) {
        ImageStream imageStream = new ImageStreamBuilder(imageStreamName).fromExternalImage(imageTag).build();
        openShift.createImageStream(imageStream);
    }

    @Override
    public void createImageStreamFromInsecureRegistry(String imageStreamName, String imageTag) {
        ImageStream imageStream = new ImageStreamBuilder(imageStreamName).insecure().fromExternalImage(imageTag).build();
        imageStream.getSpec().getTags().forEach(tag -> {
            tag.getReferencePolicy().setType(TagReferencePolicyType.LOCAL.toString());
        });
        openShift.createImageStream(imageStream);
    }

    @Override
    public String runOcCommand(String... args) {
        final String output = openShiftBinaryClient().execute(args);

        return output;
    }

    @Override
    public String runOcCommandAsAdmin(String... args) {
        final String output = openShiftBinaryClientAsAdmin().execute(args);

        return output;
    }

    @Override
    public void close() {
        try {
            openShift.close();
        } catch (Exception e) {
            logger.warn("Exception while closing OpenShift client.", e);
        }
    }

    private static synchronized OpenShiftBinary getOpenShiftBinary(String namespace) {
        return OpenShifts.masterBinary(namespace);
    }

    private static synchronized String getOpenShiftBinaryPath() {
        return OpenShifts.getBinaryPath();
    }

    @Override
    public List<Instance> getAllInstances() {
        return openShift
                .getPods()
                .stream()
                .filter(this::isScheduledPod)
                .map(pod -> OpenshiftInstanceUtil.createInstance(openShift, getName(), pod))
                .collect(toList());
    }

    private boolean isScheduledPod(Pod pod) {
        return !POD_STATUS_PENDING.equals(pod.getStatus().getPhase());
    }
}

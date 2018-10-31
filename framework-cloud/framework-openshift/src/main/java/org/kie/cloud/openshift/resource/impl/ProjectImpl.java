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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cz.xtf.openshift.OpenShiftBinaryClient;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.builder.ImageStreamBuilder;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.ImageStream;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectImpl implements Project {

    private static final Logger logger = LoggerFactory.getLogger(ProjectImpl.class);

    private String projectName;
    private OpenShiftUtil util;

    public ProjectImpl(String projectName) {
        this.projectName = projectName;
        this.util = OpenShiftController.getOpenShiftUtil(projectName);
    }

    @Override
    public String getName() {
        return projectName;
    }

    public OpenShiftUtil getOpenShiftUtil() {
        return util;
    }

    @Override
    public void delete() {
        util.deleteProject();
    }

    @Override
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables) {
        boolean templateIsFile = templateUrl.getProtocol().equals("file");
        OpenShiftBinaryClient instance = getOpenShiftBinaryClient();

        List<String> commandParameters = new ArrayList<>();
        commandParameters.add(instance.getOcBinaryPath().toString());
        commandParameters.add("--config=" + instance.getOcConfigPath().toString());
        commandParameters.add("process");
        commandParameters.add("-f");
        commandParameters.add(templateIsFile ? templateUrl.getPath() : templateUrl.toExternalForm());
        commandParameters.add("--local");
        commandParameters.add("--ignore-unknown-parameters=true");
        commandParameters.add("-o");
        commandParameters.add("yaml");
        for (Entry<String, String> envVariable : envVariables.entrySet() ) {
            commandParameters.add("-p");
            commandParameters.add(envVariable.getKey() + "=" + envVariable.getValue());
        }
        String completeProcessingCommand = commandParameters.stream().collect(Collectors.joining(" "));

        try (ProcessExecutor executor = new ProcessExecutor()) {
            File processedTemplate = executor.executeProcessCommandToTempFile(completeProcessingCommand);
            executor.executeProcessCommand(instance.getOcBinaryPath().toString() + " --config=" + instance.getOcConfigPath().toString() + " create -n " + getName() + " -f " + processedTemplate.getAbsolutePath());
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
    public void createResources(String resourceUrl) {
        try {
            KubernetesList resourceList = util.client().lists().inNamespace(projectName).load(new URL(resourceUrl)).get();
            util.client().lists().inNamespace(projectName).create(resourceList);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        }
    }

    @Override
    public void createResources(InputStream inputStream) {
        KubernetesList resourceList = util.client().lists().inNamespace(projectName).load(inputStream).get();
        util.client().lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void createImageStream(String imageStreamName, String imageTag) {
        ImageStream driverImageStream = new ImageStreamBuilder(imageStreamName).fromExternalImage(imageTag).build();
        util.createImageStream(driverImageStream);
    }

    public void close() {
        try {
            util.close();
        } catch (Exception e) {
            logger.warn("Exception while closing OpenShift client.", e);
        }
    }

    private static synchronized OpenShiftBinaryClient getOpenShiftBinaryClient() {
        return OpenShiftBinaryClient.getInstance();
    }
}

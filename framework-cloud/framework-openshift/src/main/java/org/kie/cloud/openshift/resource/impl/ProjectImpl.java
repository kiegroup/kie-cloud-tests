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
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.xtf.builder.builders.ImageStreamBuilder;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.ImageStream;

public class ProjectImpl implements Project {

    private static final Logger logger = LoggerFactory.getLogger(ProjectImpl.class);

    private String projectName;
    private OpenShift openShift;

    public ProjectImpl(String projectName) {
        this.projectName = projectName;
        this.openShift = OpenShiftController.getOpenShift(projectName);
    }

    @Override
    public String getName() {
        return projectName;
    }

    public OpenShift getOpenShift() {
        return openShift;
    }

    @Override
    public void delete() {
        openShift.deleteProject();
    }

    @Override
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables) {
        boolean templateIsFile = templateUrl.getProtocol().equals("file");

        // Used to log into OpenShift
        getOpenShiftBinary(getName());

        List<String> commandParameters = new ArrayList<>();
        commandParameters.add(getOpenShiftBinaryPath());
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
            executor.executeProcessCommand(getOpenShiftBinaryPath() + " create -n " + getName() + " -f " + processedTemplate.getAbsolutePath());
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

    static Semaphore semaphore = new Semaphore(1);

    @Override
    public synchronized void processApbRun(String image, Map<String, String> extraVars) {
        String podName = "apb-pod-" + UUID.randomUUID().toString().substring(0, 4);

        try {
            semaphore.acquire();
            OpenShiftBinary oc = getOpenShiftBinary(projectName);
            if(openShift.getServiceAccount("apb") == null) {
                oc.execute("create", "serviceaccount", "apb");
                oc.execute("create", "rolebinding", "apb", "--clusterrole=admin", "--serviceaccount=" + projectName + ":apb");
            }

            List<String> args = new ArrayList<>();
            args.add("run");
            args.add(podName);
            // args.add("--namespace=" + projectName);
            args.add("--env=POD_NAME=" + podName);
            args.add("--env=POD_NAMESPACE=" + projectName);
            args.add("--image=" + image);
            args.add("--restart=Never");
            args.add("--attach=true");
            args.add("--serviceaccount=apb");
            args.add("--");
            args.add("provision");
            args.add("--extra-vars");
            args.add(formatExtraVars(extraVars));

            logger.info("Executing command: oc " + getApbCommand(args));
            oc.execute(args.toArray(new String[0]));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for scenario to be initialized.", e);
        } finally {
            semaphore.release();
        }
    }

    private String getApbCommand(List<String> args) {
        return args.stream().collect(Collectors.joining(" "));
    }

    private String formatExtraVars(Map<String, String> extraVars) {
        return extraVars.entrySet()
                        .stream()
                        .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
                        .collect(Collectors.joining(", ", "{", "}"));
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
    public void createResources(InputStream inputStream) {
        KubernetesList resourceList = openShift.lists().inNamespace(projectName).load(inputStream).get();
        openShift.lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void createImageStream(String imageStreamName, String imageTag) {
        ImageStream driverImageStream = new ImageStreamBuilder(imageStreamName).fromExternalImage(imageTag).build();
        openShift.createImageStream(driverImageStream);
    }

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
}

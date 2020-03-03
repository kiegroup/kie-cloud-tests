/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.openshift.deployment;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerStateRunning;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.kie.cloud.api.deployment.CommandExecutionResult;
import org.kie.cloud.api.deployment.Instance;
import rx.Observable;
import rx.observables.StringObservable;

import static org.kie.cloud.openshift.util.CommandUtil.runCommandImpl;

public class OpenShiftInstance implements Instance {

    private OpenShift openshift;
    private String name;
    private String namespace;

    public OpenShiftInstance(OpenShift openshift, String namespace, String name) {
        this.openshift = openshift;
        this.name = name;
        this.namespace = namespace;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public OpenShift getOpenShift() {
        return openshift;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandExecutionResult runCommand(String... command) {
        return runCommandImpl(openshift.pods().withName(name), command);
    }

    @Override
    public boolean isRunning() {
        return exists() && getContainerRunningState() != null;
    }

    @Override
    public boolean exists() {
        return Objects.nonNull(openshift.getPod(name));
    }

    @Override
    public Instant startedAt() throws IllegalStateException {
        if (!isRunning()) {
            throw new IllegalStateException("Instance is not in running state, started time not available.");
        }

        String startedAt = getContainerRunningState().getStartedAt();
        return Instant.parse(startedAt);
    }

    private ContainerStateRunning getContainerRunningState() {
        Pod pod = openshift.getPod(name);
        return pod.getStatus().getContainerStatuses().get(0).getState().getRunning();
    }

    @Override
    public String getLogs() {
        // Get logs from first container (or null if none ?...)
        return getLogs(getContainers()
                                      .stream()
                                      .findFirst()
                                      .map(Container::getName)
                                      .orElse(null));
    }

    /**
     * Return a map (containerName/logs) of all containers logs from the pod
     * @return
     */
    public Map<String, String> getAllContainerLogs() {
        return getContainers()
                              .stream()
                              .map(Container::getName)
                              .collect(Collectors.toMap(Function.identity(), this::getLogs));
    }

    /**
     * Return logs from a specific container of the pod
     * @param containerName
     * @return
     */
    public String getLogs(String containerName) {
        if (Objects.nonNull(containerName)) {
            return openshift.pods().withName(name).inContainer(containerName).getLog();
        } else {
            return openshift.getPodLog(openshift.getPod(name));
        }
    }

    private List<Container> getContainers() {
        return Optional.ofNullable(openshift.getPod(name))
                       .map(Pod::getSpec)
                       .map(PodSpec::getContainers)
                       .orElse(new ArrayList<>());
    }

    public Map<String, Observable<String>> observeAllContainersLogs() {
        return getContainers().stream()
                              .map(Container::getName)
                              .collect(Collectors.toMap(Function.identity(), this::observeContainerLogs));
    }

    public Observable<String> observeContainerLogs(String containerName) {
        if (Objects.nonNull(containerName)) {
            LogWatch watcher = openshift.pods().withName(name).inContainer(containerName).watchLog();
            return StringObservable.byLine(StringObservable.from(new InputStreamReader(watcher.getOutput())));
        } else {
            return openshift.observePodLog(openshift.getPod(name));
        }
    }
}

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

import static org.kie.cloud.openshift.util.CommandUtil.runCommandImpl;

import java.time.Instant;

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.ContainerStateRunning;
import io.fabric8.kubernetes.api.model.Pod;

import org.kie.cloud.api.deployment.CommandExecutionResult;
import org.kie.cloud.api.deployment.Instance;

public class OpenShiftInstance implements Instance {

    private OpenShiftUtil util;
    private String name;
    private String namespace;

    public OpenShiftInstance(OpenShiftUtil util, String namespace, String name) {
        this.util = util;
        this.name = name;
        this.namespace = namespace;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public OpenShiftUtil getOpenShiftUtil() {
        return util;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override public CommandExecutionResult runCommand(String... command) {
        return runCommandImpl(util.client().pods().withName(name), command);
    }

    @Override
    public boolean isRunning() {
        return getContainerRunningState() != null;
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
        Pod pod = util.getPod(name);
        return pod.getStatus().getContainerStatuses().get(0).getState().getRunning();
    }

    @Override
    public String getLogs() {
        Pod pod = util.getPod(name);
        return util.getPodLog(pod);
    }
}

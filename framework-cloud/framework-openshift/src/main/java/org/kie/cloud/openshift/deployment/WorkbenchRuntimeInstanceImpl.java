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

package org.kie.cloud.openshift.deployment;

import static org.kie.cloud.openshift.util.CommandUtil.runCommandImpl;

import java.io.ByteArrayOutputStream;

import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.kie.cloud.api.deployment.CommandExecutionResult;
import org.kie.cloud.api.deployment.WorkbenchInstance;
import org.kie.cloud.openshift.OpenShiftController;

public class WorkbenchRuntimeInstanceImpl implements WorkbenchInstance {

    private OpenShiftController openShiftController;
    private String namespace;
    private String podName;

    public OpenShiftController getOpenShiftController() {
        return openShiftController;
    }

    @Override public String getNamespace() {
        return namespace;
    }

    @Override public CommandExecutionResult runCommand(String... command) {
        return runCommandImpl(openShiftController.getClient().pods().inNamespace(namespace).withName(podName), command);
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setOpenShiftController(OpenShiftController openShiftController) {
        this.openShiftController = openShiftController;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    @Override public String getName() {
        return podName;
    }

    @Override public String getLogs() {
        return openShiftController.getClient().pods().inNamespace(namespace).withName(podName).getLog();
    }
}

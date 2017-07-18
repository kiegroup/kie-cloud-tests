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

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.DeploymentConfig;

public class DeploymentConfigImpl implements DeploymentConfig {

    private OpenShiftClient client;
    private String projectName;
    private String deploymentConfigName;

    public DeploymentConfigImpl(OpenShiftClient client, String projectName, String deploymentConfigName) {
        this.client = client;
        this.projectName = projectName;
        this.deploymentConfigName = deploymentConfigName;
    }

    @Override
    public String getName() {
        return deploymentConfigName;
    }

    @Override
    public void delete() {
        client.deploymentConfigs().inNamespace(projectName).withName(deploymentConfigName).delete();
    }

    @Override
    public void scalePods(int numberOfPods) {
        client.deploymentConfigs().inNamespace(projectName).withName(deploymentConfigName).scale(numberOfPods, true);
        // Wait flag while scaling of deployment config doesn't seem to work correctly, use own waiting functionality
        waitUntilAllPodsAreReady();
    }

    @Override
    public void waitUntilAllPodsAreReady() {
        waitUntilAllPodsAreStarted();
        waitUntilAllPodsAreRunning();
    }

    private void waitUntilAllPodsAreStarted() {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + OpenShiftConstants.DEPLOYMENT_PODS_TERMINATION_TIMEOUT;
        int expectedPods = client.deploymentConfigs().inNamespace(projectName).withName(deploymentConfigName).get().getSpec().getReplicas().intValue();

        while(Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            if(client.pods().inNamespace(projectName).withLabel(OpenShiftConstants.DEPLOYMENT_CONFIG_LABEL, deploymentConfigName).list().getItems().size() == expectedPods) {
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for pods to start.", e);
            }
        }
        throw new RuntimeException("Timeout while waiting for pods to start.");
    }

    private void waitUntilAllPodsAreRunning() {
        List<Pod> pods = client.pods().inNamespace(projectName).withLabel(OpenShiftConstants.DEPLOYMENT_CONFIG_LABEL, deploymentConfigName).list().getItems();
        for(Pod pod : pods) {
            try {
                client.pods().inNamespace(projectName).withName(pod.getMetadata().getName()).waitUntilReady(OpenShiftConstants.PODS_START_TO_READY_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for pod to be ready.", e);
            }
        }
    }
}

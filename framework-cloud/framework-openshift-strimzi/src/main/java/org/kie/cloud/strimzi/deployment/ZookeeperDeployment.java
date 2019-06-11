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

package org.kie.cloud.strimzi.deployment;

import java.util.concurrent.TimeUnit;

import cz.xtf.core.waiting.SupplierWaiter;
import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.openshift.deployment.OpenShiftDeployment;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;

public class ZookeeperDeployment extends OpenShiftDeployment {

    private static final String STATEFUL_SET_NAME_SUFFIX = "-zookeeper";
    private static final String SERVICE_NAME_SUFFIX = "-zookeeper-client";
    private static final String POD_LABEL_KEY = "strimzi.io/name";
    private static final String ZOOKEEPER_POD_LABEL_VALUE_SUFFIX = "-zookeeper";

    private String clusterName;

    public ZookeeperDeployment(String clusterName, Project project) {
        super(project);

        this.clusterName = clusterName;
    }

    @Override
    public void waitForScale() {
        SupplierWaiter statefulSetExistWaitter = new SupplierWaiter(
                () -> getOpenShift().apps().statefulSets().withName(clusterName + STATEFUL_SET_NAME_SUFFIX).get(),
                x -> x != null,
                x -> false,
                TimeUnit.MILLISECONDS,
                OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT);
        statefulSetExistWaitter.waitFor();

        int expectedPods = getOpenShift().apps().statefulSets().withName(clusterName + STATEFUL_SET_NAME_SUFFIX)
                .get().getSpec().getReplicas();
        waitUntilAllPodsAreReadyAndRunning(expectedPods);
    }

    @Override
    protected void waitUntilAllPodsAreReadyAndRunning(int expectedPods) {
        try {
            getOpenShift().waiters()
                    .areExactlyNPodsReady(expectedPods, POD_LABEL_KEY, clusterName + ZOOKEEPER_POD_LABEL_VALUE_SUFFIX)
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .reason("Waiting for " + expectedPods + " pods of stateful set " +
                                    clusterName + STATEFUL_SET_NAME_SUFFIX + " to become ready.")
                    .waitFor();

            getOpenShift().waiters()
                    .areExactlyNPodsRunning(expectedPods, POD_LABEL_KEY, clusterName + ZOOKEEPER_POD_LABEL_VALUE_SUFFIX)
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .reason("Waiting for " + expectedPods + " pods of stateful set " +
                                    clusterName + STATEFUL_SET_NAME_SUFFIX + " to become runnning.")
                    .waitFor();
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to start.");
        }
    }

    @Override
    public String getServiceName() {
        return clusterName + SERVICE_NAME_SUFFIX;
    }
}

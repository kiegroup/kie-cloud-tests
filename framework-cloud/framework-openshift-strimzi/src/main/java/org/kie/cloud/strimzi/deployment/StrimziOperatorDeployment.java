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

public class StrimziOperatorDeployment extends OpenShiftDeployment {

    private static final String STRIMZI_DEPLOYMENT = "strimzi-cluster-operator";
    private static final String POD_LABEL_KEY = "name";
    private static final String POD_LABEL_VALUE = "strimzi-cluster-operator";

    public StrimziOperatorDeployment(Project project) {
        super(project);
    }

    @Override
    public void waitForScale() {
        SupplierWaiter deploymentExistWaitter = new SupplierWaiter(
                () -> getOpenShift().apps().deployments().withName(STRIMZI_DEPLOYMENT).get(),
                x -> x != null,
                x -> false,
                TimeUnit.MILLISECONDS,
                OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT);
        deploymentExistWaitter.waitFor();

        int expectedPods = getOpenShift().apps().deployments().withName(STRIMZI_DEPLOYMENT)
                .get().getSpec().getReplicas();
        waitUntilAllPodsAreReadyAndRunning(expectedPods);
    }

    @Override
    protected void waitUntilAllPodsAreReadyAndRunning(int expectedPods) {
        try {
            getOpenShift().waiters()
                    .areExactlyNPodsReady(expectedPods, POD_LABEL_KEY, POD_LABEL_VALUE)
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .reason("Waiting for " + expectedPods + " pods of Strimzi operator to become ready.")
                    .waitFor();

            getOpenShift().waiters()
                    .areExactlyNPodsRunning(expectedPods, POD_LABEL_KEY, POD_LABEL_VALUE)
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .reason("Waiting for " + expectedPods + " pods of Strimzi operator to become runnning.")
                    .waitFor();
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to start.");
        }
    }

    @Override
    public String getServiceName() {
        throw new UnsupportedOperationException("There is no service associated with Strimzi operator");
    }
}

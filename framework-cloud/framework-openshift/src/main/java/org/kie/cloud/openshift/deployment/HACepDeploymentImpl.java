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

package org.kie.cloud.openshift.deployment;

import java.net.URL;

import org.kie.cloud.api.deployment.DeploymentTimeoutException;
import org.kie.cloud.api.deployment.HACepDeployment;
import org.kie.cloud.openshift.resource.OpenShiftResourceConstants;
import org.kie.cloud.openshift.resource.Project;

public class HACepDeploymentImpl extends OpenShiftDeployment implements HACepDeployment {

    private static final String DEPLOYMENT_NAME = "openshift-kie-springboot";
    private static final String SERVICE_NAME = "openshift-kie-springboot";
    private static final String POD_LABEL_KEY = "app";
    private static final String POD_LABEL_VALUE = "openshift-kie-springboot";

    public HACepDeploymentImpl(final Project project) {
        super(project);
    }

    @Override
    public void waitForScale() {
        int expectedPods = getOpenShift().apps().deployments().withName(DEPLOYMENT_NAME).get().getSpec().getReplicas();
        waitUntilAllPodsAreReadyAndRunning(expectedPods);
    }

    @Override
    protected void waitUntilAllPodsAreReadyAndRunning(int expectedPods) {
        try {
            getOpenShift().waiters()
                    .areExactlyNPodsReady(expectedPods, POD_LABEL_KEY, POD_LABEL_VALUE)
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .reason("Waiting for " + expectedPods + " pods of service " + getServiceName() + " to become ready.")
                    .waitFor();

            getOpenShift().waiters()
                    .areExactlyNPodsRunning(expectedPods, POD_LABEL_KEY, POD_LABEL_VALUE)
                    .timeout(OpenShiftResourceConstants.PODS_START_TO_READY_TIMEOUT)
                    .reason("Waiting for " + expectedPods + " pods of service " + getServiceName() + " to become runnning.")
                    .waitFor();
        } catch (AssertionError e) {
            throw new DeploymentTimeoutException("Timeout while waiting for pods to start.");
        }
    }

    @Override
    public URL getUrl() {
        return getHttpRouteUrl(getServiceName())
                .orElseThrow(() -> new RuntimeException("Can not found route for service: " + getServiceName()));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}

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

package org.kie.cloud.openshift.operator.scenario;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import cz.xtf.client.Http;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.operator.deployment.OperatorConsoleDeployment;

/**
 * Scenario used when no custom resources should be deployed. OpenShift project will contain just installed operator.
 */
public class EmptyOperatorScenario extends OpenShiftOperatorScenario<EmptyOperatorScenario> {

    public EmptyOperatorScenario() {
        super(null);
    }

    private OperatorConsoleDeployment consoleDeployment;

    @Override
    protected void deployCustomResource() {
        consoleDeployment = new OperatorConsoleDeployment(project);
        waitForOperatorConsole();
        // No resources to be deployed as this is empty scenario.
    }

    /**
     * Wait until Operator console starts and is available using route.
     */
    private void waitForOperatorConsole() {
        try {
            Http.get(getConsoleDeploymentUrl().toExternalForm()).trustAll().waiters().code(403).waitFor();
        } catch (IOException e) {
            throw new RuntimeException("Error while waiting for Operator console to start.", e);
        }
    }

    public URL getConsoleDeploymentUrl() {
        return consoleDeployment.getHttpsRouteUrl();
    }

    @Override
    public List<Deployment> getDeployments() {
        return Collections.emptyList();
    }
}

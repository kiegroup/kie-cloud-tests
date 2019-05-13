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

package org.kie.cloud.openshift.operator.deployment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import io.fabric8.openshift.api.model.Route;
import org.kie.cloud.openshift.resource.Project;

public class OperatorConsoleDeployment {

    private Project project;

    public OperatorConsoleDeployment(Project project) {
        this.project = project;
    }

    public URL getHttpsRouteUrl() {
        Optional<Route> operatorConsoleRoute = project.getOpenShift().getRoutes().stream()
                                                                                 .filter(n -> n.getSpec().getTls() != null)
                                                                                 .filter(n -> n.getMetadata().getLabels().get("name").equals("console-cr-form"))
                                                                                 .findAny();

        String routeUrl = operatorConsoleRoute.map(route -> "https://" + route.getSpec().getHost() + ":443")
                                              .orElseThrow(() -> new RuntimeException("Kie Operator console route not found."));
        try {
            return new URL(routeUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed Kie Operator console URL: " + routeUrl);
        }
    }
}
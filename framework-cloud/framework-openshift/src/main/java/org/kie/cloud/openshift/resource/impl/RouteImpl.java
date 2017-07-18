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

import java.util.List;

import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.cloud.openshift.resource.Route;

public class RouteImpl implements Route {

    private OpenShiftClient client;
    private String projectName;
    private String serviceName;

    public RouteImpl(OpenShiftClient client, String projectName, String serviceName) {
        this.client = client;
        this.projectName = projectName;
        this.serviceName = serviceName;
    }

    @Override
    public String getRouteHost() {
        List<io.fabric8.openshift.api.model.Route> routes = client.routes().inNamespace(projectName).list().getItems();
        for(io.fabric8.openshift.api.model.Route route : routes) {
            if(route.getSpec().getTo().getName().equals(serviceName)) {
                return route.getSpec().getHost();
            }
        }
        throw new RuntimeException(String.format("Route for service '%s' not found.", serviceName));
    }

    @Override
    public void delete() {
        client.routes().inNamespace(projectName).withName(serviceName).delete();
    }
}

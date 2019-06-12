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

package org.kie.cloud.openshift.prometheus.servicemonitor.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Spec.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Spec {

    private Selector selector;
    private List<Endpoint> endpoints = new ArrayList<>();

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    public Endpoint[] getEndpoints() {
        return endpoints.toArray(new Endpoint[0]);
    }

    public void setEndpoints(Endpoint[] endpoints) {
        this.endpoints = Arrays.asList(endpoints);
    }
}

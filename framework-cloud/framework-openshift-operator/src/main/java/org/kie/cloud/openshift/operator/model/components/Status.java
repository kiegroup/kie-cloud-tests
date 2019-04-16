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

package org.kie.cloud.openshift.operator.model.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Custom resource status. 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Status {

    private Conditions[] conditions;
    private String consoleHost;
    private String[] deployments;

    public Conditions[] getConditions() {
        return conditions;
    }

    public void setConditions(Conditions[] conditions) {
        this.conditions = conditions;
    }

    public String getConsoleHost() {
        return consoleHost;
    }

    public void setConsoleHost(String consoleHost) {
        this.consoleHost = consoleHost;
    }

    public String[] getDeployments() {
        return deployments;
    }

    public void setDeployments(String[] deployments) {
        this.deployments = deployments;
    }
}

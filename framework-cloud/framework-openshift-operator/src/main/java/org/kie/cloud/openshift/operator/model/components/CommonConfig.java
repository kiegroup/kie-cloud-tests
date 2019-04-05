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
 * General KieApp configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CommonConfig {

    private String adminPassword;
    private String dbPassword;
    private String amqPassword;
    private String amqClusterPassword;
    private String controllerPassword;
    private String serverPassword;
    private String mavenPassword;

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAmqClusterPassword() {
        return amqClusterPassword;
    }

    public void setAmqClusterPassword(String amqClusterPassword) {
        this.amqClusterPassword = amqClusterPassword;
    }

    public String getAmqPassword() {
        return amqPassword;
    }

    public void setAmqPassword(String amqPassword) {
        this.amqPassword = amqPassword;
    }

    public String getControllerPassword() {
        return controllerPassword;
    }

    public void setControllerPassword(String controllerPassword) {
        this.controllerPassword = controllerPassword;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public String getMavenPassword() {
        return mavenPassword;
    }

    public void setMavenPassword(String mavenPassword) {
        this.mavenPassword = mavenPassword;
    }
}

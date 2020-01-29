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

    private String adminUser;
    private String adminPassword;
    private String amqClusterPassword;
    private String amqPassword;
    private String applicationName;
    private String controllerPassword;
    private String dbPassword;
    private String imageTag;
    private String keyStorePassword;
    private String mavenPassword;
    private String serverPassword;
    private String version;

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getAmqClusterPassword() {
        return amqClusterPassword;
    }

    public String getAmqPassword() {
        return amqPassword;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getControllerPassword() {
        return controllerPassword;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getImageTag() {
        return imageTag;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getMavenPassword() {
        return mavenPassword;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public String getVersion() {
        return version;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setAmqClusterPassword(String amqClusterPassword) {
        this.amqClusterPassword = amqClusterPassword;
    }

    public void setAmqPassword(String amqPassword) {
        this.amqPassword = amqPassword;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setControllerPassword(String controllerPassword) {
        this.controllerPassword = controllerPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setMavenPassword(String mavenPassword) {
        this.mavenPassword = mavenPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

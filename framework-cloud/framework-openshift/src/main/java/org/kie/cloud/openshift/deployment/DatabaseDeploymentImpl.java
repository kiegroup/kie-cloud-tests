/*
 * Copyright 2017 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Optional;

import org.kie.cloud.api.deployment.DatabaseDeployment;
import org.kie.cloud.openshift.resource.Project;

public class DatabaseDeploymentImpl extends OpenShiftDeployment implements DatabaseDeployment {

    private String username;
    private String password;
    private String databaseName;
    private Optional<URL> url;
    private String serviceName;
    private String serviceSuffix = "";

    public DatabaseDeploymentImpl(Project project) {
        super(project);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabaseName(String database) {
        this.databaseName = database;
    }

    public void setUrl(URL url) {
        this.url = Optional.of(url);
    }

    @Override
    public Optional<URL> getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setServiceSuffix(String serviceSuffix) {
        this.serviceSuffix = serviceSuffix;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getDatabaseServiceName(getOpenShiftUtil(), serviceSuffix);
        }
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}

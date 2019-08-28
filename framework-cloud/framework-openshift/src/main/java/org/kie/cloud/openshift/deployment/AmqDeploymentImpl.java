/*
 * Copyright 2019 JBoss by Red Hat.
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

import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.openshift.resource.Project;

public class AmqDeploymentImpl extends OpenShiftDeployment implements AmqDeployment {

    private String serviceName, tcpServiceName;
    private Optional<URL> secureUrl;
    private Optional<URL> tcpSslUrl;
    private String username;
    private String password;

    public AmqDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getAmqJolokiaServiceName(getOpenShift());
        }
        return serviceName;
    }

    private String getAmqTcpSslServiceName() {
        if (tcpServiceName == null) {
            tcpServiceName = ServiceUtil.getAmqTcpSslServiceName(getOpenShift());
        }
        return tcpServiceName;
    }

    @Override
    public URL getTcpSslUrl() {
        if (tcpSslUrl == null) {
            tcpSslUrl = getHttpsRouteUrl(getAmqTcpSslServiceName());
        }
        return tcpSslUrl.orElseThrow(() -> new RuntimeException("No Amq SSL URL is available."));
    }

    @Override
    public URL getUrl() {
        return getSecureUrl().orElseThrow(() -> new RuntimeException("No Amq URL is available."));
    }

    @Override
    public Optional<URL> getSecureUrl() {
        if (secureUrl == null) {
            secureUrl = getHttpsRouteUrl(getServiceName());
        }
        return secureUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }

}

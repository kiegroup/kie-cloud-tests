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

import org.kie.cloud.api.deployment.AmqDeployment;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;

public class AmqDeploymentImpl extends OpenShiftDeployment implements AmqDeployment {

    private String amqJolokiaServiceName;
    private String tcpServiceName;
    private URL amqJolokiaUrl;
    private URL tcpSslUrl;
    private String username;
    private String password;

    public AmqDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        return getAmqTcpSslServiceName();
    }

    @Override
    public String getDeploymentConfigName() {
        // TODO: convert to some reliable openshift resource selector
        return OpenShiftConstants.getKieApplicationName() + "-amq";
    }

    private String getAmqJolokiaServiceName() {
        if (amqJolokiaServiceName == null) {
            amqJolokiaServiceName = ServiceUtil.getAmqJolokiaServiceName(getOpenShift());
        }
        return amqJolokiaServiceName;
    }

    private String getAmqTcpSslServiceName() {
        if (tcpServiceName == null) {
            tcpServiceName = ServiceUtil.getAmqTcpSslServiceName(getOpenShift());
        }
        return tcpServiceName;
    }

    @Override
    public URL getAmqJolokiaUrl() {
        if (amqJolokiaUrl == null) {
            amqJolokiaUrl = getHttpsRouteUrl(getAmqJolokiaServiceName()).orElseThrow(() -> new RuntimeException("No Amq Jolokia SSL URL is available."));
        }
        return amqJolokiaUrl;
    }

    @Override
    public URL getTcpSslUrl() {
        if (tcpSslUrl == null) {
            tcpSslUrl = getHttpsRouteUrl(getAmqTcpSslServiceName()).orElseThrow(() -> new RuntimeException("No Amq SSL URL is available."));
        }
        return tcpSslUrl;
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
        // Skip waiting for router as we cannot process AMQ certificate at this moment.
//        if (!getInstances().isEmpty()) {
//            RouterUtil.waitForRouter(getAmqJolokiaUrl());
//        }
    }

}

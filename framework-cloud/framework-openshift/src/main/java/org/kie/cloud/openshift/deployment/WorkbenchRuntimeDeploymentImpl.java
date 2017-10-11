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

package org.kie.cloud.openshift.deployment;

import java.net.URL;

import org.kie.cloud.api.deployment.WorkbenchDeployment;

public class WorkbenchRuntimeDeploymentImpl extends OpenShiftDeployment implements WorkbenchDeployment {

    private URL url;
    private URL secureUrl;
    private String username;
    private String password;

    private String serviceName;
    private String secureServiceName;

    @Override public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(serviceName);
        }
        return url;
    }

    @Override public URL getSecureUrl() {
        if (secureUrl == null) {
            secureUrl = getHttpsRouteUrl(secureServiceName);
        }
        return secureUrl;
    }

    @Override public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String applicationName) {
        this.serviceName = applicationName + "-buscentrmon";
    }

    public String getSecureServiceName() {
        return secureServiceName;
    }

    public void setSecureServiceName(String applicationName) {
        this.secureServiceName = "secure-" + applicationName + "-buscentrmon";
    }

    @Override public void waitForScale() {
        openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().waitUntilAllPodsAreReady();
        if (openShiftController.getProject(namespace).getService(getServiceName()).getDeploymentConfig().podsNumber() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }
}

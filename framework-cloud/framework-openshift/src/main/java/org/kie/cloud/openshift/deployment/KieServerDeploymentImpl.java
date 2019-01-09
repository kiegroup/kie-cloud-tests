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
import java.util.Optional;

import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.openshift.resource.Project;

public class KieServerDeploymentImpl extends OpenShiftDeployment implements KieServerDeployment {

    private Optional<URL> url;
    private Optional<URL> secureUrl;
    private String username;
    private String password;

    private String serviceName;
    private String serviceSuffix = "";

    public KieServerDeploymentImpl(Project project) {
        super(project);
    }

    @Override public Optional<URL> getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName());
        }
        return url;
    }

    @Override public Optional<URL> getSecureUrl() {
        if (secureUrl == null) {
            secureUrl = getHttpsRouteUrl(getServiceName());
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

    public void setServiceSuffix(String serviceSuffix) {
        this.serviceSuffix = serviceSuffix;
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getKieServerServiceName(getOpenShiftUtil(), serviceSuffix);
        }
        return serviceName;
    }

    @Override public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl().orElseGet(getSecureUrl()::get));
        }
    }
}
/*
 * Copyright 2018 JBoss by Red Hat.
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
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.api.deployment.ControllerDeployment;

public class ControllerDeploymentImpl extends OpenShiftDeployment implements ControllerDeployment {

    private URL url;
    private String username;
    private String password;

    private String serviceName;

    public ControllerDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName());
        }
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getControllerServiceName(getOpenShiftUtil());
        }
        return serviceName;
    }

    @Override
    public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }
}

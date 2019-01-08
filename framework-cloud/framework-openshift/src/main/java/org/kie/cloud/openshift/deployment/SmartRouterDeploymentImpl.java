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

import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.openshift.resource.Project;

public class SmartRouterDeploymentImpl extends OpenShiftDeployment implements SmartRouterDeployment {

    private URL url;

    private String serviceName;

    public SmartRouterDeploymentImpl(Project project) {
        super(project);
    }

    @Override public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(serviceName);
        }
        return url;
    }

    @Override
    public String getServiceName() {
        if(serviceName == null) {
            serviceName = ServiceUtil.getSmartRouterServiceName(getOpenShiftUtil());
        }
        return serviceName;
    }

    public void setServiceName(String applicationName) {
        this.serviceName = applicationName + "-smartrouter";
    }

    @Override public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }
}

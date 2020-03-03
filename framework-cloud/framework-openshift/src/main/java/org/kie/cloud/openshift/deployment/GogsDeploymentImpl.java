/*
 * Copyright 2020 JBoss by Red Hat.
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

import org.kie.cloud.api.deployment.GogsDeployment;
import org.kie.cloud.openshift.resource.Project;

public class GogsDeploymentImpl extends OpenShiftDeployment implements GogsDeployment {

    private static final String USERNAME = "root";
    private static final String PASSWORD = "redhat";

    private String serviceName;
    private URL url;

    public GogsDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getGogsServiceName(getOpenShift());
        }

        return serviceName;
    }

    @Override
    public String getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName()).orElseThrow(() -> new RuntimeException("No Gogs URL is available."));
        }

        return url.toString();
    }

    @Override
    public String getUsername() {
        return USERNAME;
    }

    @Override
    public String getPassword() {
        return PASSWORD;
    }
}

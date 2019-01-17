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

import org.kie.cloud.api.deployment.DockerDeployment;
import org.kie.cloud.openshift.resource.Project;

public class DockerDeploymentImpl extends OpenShiftDeployment implements DockerDeployment {

    private String serviceName;
    private URL url;

    public DockerDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getDockerServiceName(getOpenShiftUtil());
        }
        return serviceName;
    }

    @Override
    public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName()).orElseThrow(() -> new RuntimeException("No Docker URL is available."));
        }
        return url;
    }

    @Override
    public void waitForScale() {
        super.waitForScale();
    }
}

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

import org.kie.cloud.api.deployment.PrometheusDeployment;
import org.kie.cloud.openshift.resource.Project;

public class PrometheusDeploymentImpl extends OpenShiftDeployment implements PrometheusDeployment {

    private String serviceName;
    private URL url;

    public PrometheusDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getPrometheusServiceName(getOpenShift());
        }
        return serviceName;
    }

    @Override
    public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName()).orElseThrow(() -> new RuntimeException("No Prometheus URL is available."));
        }
        return url;
    }

    @Override
    public void waitForScale() {
        throw new UnsupportedOperationException("Not supported as Prometheus deployment is currently a stateful set.");
    }
}

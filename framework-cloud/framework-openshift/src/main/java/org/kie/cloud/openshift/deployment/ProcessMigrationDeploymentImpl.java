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
import java.util.Optional;

import org.kie.cloud.api.deployment.ProcessMigrationDeployment;
import org.kie.cloud.openshift.resource.Project;

public class ProcessMigrationDeploymentImpl extends OpenShiftDeployment implements ProcessMigrationDeployment {

    private Optional<URL> insecureUrl = Optional.empty();
    private Optional<URL> secureUrl = Optional.empty();

    private String serviceName;

    public ProcessMigrationDeploymentImpl(Project project) {
        super(project);
    }   

    @Override
    public URL getUrl() {
        return getInsecureUrl().orElseGet(() -> getSecureUrl().orElseThrow(() -> new RuntimeException("No Process Migration URL is available.")));
    }

    public Optional<URL> getInsecureUrl() {
        if (!insecureUrl.isPresent()) {
            insecureUrl = getHttpRouteUrl(getServiceName());
        }
        return insecureUrl;
    }

    public Optional<URL> getSecureUrl() {
        if (!secureUrl.isPresent()) {
            secureUrl = getHttpsRouteUrl(getServiceName());
        }
        return secureUrl;
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getProcessMigrationServiceName(getOpenShift());
        }
        return serviceName;
    }

    @Override
    public void waitForScale() {
        super.waitForScale();
        if (!getInstances().isEmpty()) {
            getInsecureUrl().ifPresent(RouterUtil::waitForRouter);
            getSecureUrl().ifPresent(RouterUtil::waitForRouter);
        }
    }

}

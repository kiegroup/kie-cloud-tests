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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.resource.Project;

public class MavenNexusRepositoryDeploymentImpl extends OpenShiftDeployment implements MavenRepositoryDeployment {

    private static final String NEXUS_PATH_SUFFIX = "/nexus/content/repositories/";
    private static final String NEXUS_RELEASES_REPO_PATH = NEXUS_PATH_SUFFIX + "releases/";
    private static final String NEXUS_SNAPSHOTS_REPO_PATH = NEXUS_PATH_SUFFIX + "snapshots/";

    private String serviceName;
    private URL url;

    public MavenNexusRepositoryDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getMavenNexusServiceName(getOpenShift());
        }
        return serviceName;
    }

    @Override
    public URL getUrl() {
        if (url == null) {
            url = getHttpRouteUrl(getServiceName()).orElseThrow(() -> new RuntimeException("No Maven Repository URL is available."));
        }
        return url;
    }

    @Override
    public URL getReleasesRepositoryUrl() {
        try {
            return getUrl().toURI().resolve(NEXUS_RELEASES_REPO_PATH).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getSnapshotsRepositoryUrl() {
        try {
            return getUrl().toURI().resolve(NEXUS_SNAPSHOTS_REPO_PATH).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername() {
        return "admin";
    }

    @Override
    public String getPassword() {
        return "admin123";
    }

    @Override
    public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }
}

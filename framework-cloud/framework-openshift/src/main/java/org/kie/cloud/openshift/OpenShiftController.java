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

package org.kie.cloud.openshift;

import java.io.Closeable;
import java.util.List;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.impl.ProjectImpl;

public class OpenShiftController implements Closeable {

    private OpenShiftClient client;

    /**
     * Create OpenShift controller using values defined by syst. properties.
     * @see https://github.com/fabric8io/kubernetes-client
     */
    public OpenShiftController() {
        client = new DefaultOpenShiftClient();
    }

    /**
     * Create OpenShift controller using specified values.
     *
     * @param openShiftMasterUrl URL to running OpenShift instance (for example https://master.openshiftdomain:8443).
     * @param username Username for logging into OpenShift.
     * @param password Password for logging into OpenShift.
     */
    public OpenShiftController(String openShiftMasterUrl, String username, String password) {
        // Trust to all certificates (even self signed ones)
        System.setProperty(Config.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true");

        Config config = new ConfigBuilder().withMasterUrl(openShiftMasterUrl).withUsername(username).withPassword(password).build();
        client = new DefaultOpenShiftClient(config);
    }

    /**
     * Create OpenShift project.
     *
     * @param projectName OpenShift project name.
     */
    public Project createProject(String projectName) {
        client.projectrequests().createNew()
                .withNewMetadata()
                    .withName(projectName)
                .endMetadata()
                .withDescription("New project " + projectName)
                .withDisplayName(projectName)
            .done();

        return new ProjectImpl(client, projectName);
    }

    /**
     * Load Openshift project
     *
     * @param projectName Open Openshift project name
     * @return Open Openshift project
     */
    public Project getProject(String projectName) {
        Project project = new ProjectImpl(client, projectName);
        List<io.fabric8.openshift.api.model.Project> projectList = client.projects().list().getItems();
        if (projectList.stream().anyMatch(p -> p.getMetadata().getName().equals(projectName)) == false) {
            throw new RuntimeException(String.format("Project with name %s not found", projectName));
        }

        return project;
    }

    /**
     * @return OpenShiftClient for tests requiring specific functionality which is not covered by framework.
     */
    public OpenShiftClient getClient() {
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}

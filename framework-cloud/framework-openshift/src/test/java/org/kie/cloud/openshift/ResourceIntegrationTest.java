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

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.Service;

public class ResourceIntegrationTest extends OpenShiftIntegrationTestBase {

    private static final String BPMS_RESOURCES_FILE = "bpms-resource-list.json";

    private Project project;

    @Before
    public void createProject() {
        project = controller.createProject(projectName);
    }

    @After
    public void deleteProject() {
        project.delete();
    }

    @Test
    public void testResourceCreation() {
        project.createResources(getResourcesUrl());

        List<Service> services = project.getServices();
        Assertions.assertThat(services).isNotNull().hasSize(2);

        List<String> serviceNames = services.stream().map(n -> n.getName()).collect(Collectors.toList());
        Assertions.assertThat(serviceNames).contains("buscentr-myapp", "buscentr-myapp2");
    }

    private String getResourcesUrl() {
        return ResourceIntegrationTest.class.getClassLoader().getResource(BPMS_RESOURCES_FILE).toString();
    }
}

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

import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.Route;
import org.kie.cloud.openshift.resource.Service;
import org.kie.cloud.openshift.resource.impl.RouteImpl;

public class RouteIntegrationTest extends OpenShiftIntegrationTestBase {

    private static final String WILDFLY_SERVICE = "wildfly-service";
    private static final String WILDFLY_IMAGE = "jboss/wildfly:10.1.0.Final";

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
    public void testRetrieveRouteHost() {
        Service wildflyService = project.createService(WILDFLY_SERVICE);
        wildflyService.createDeploymentConfig(WILDFLY_IMAGE, new HashMap<String, String>());
        Route wildflyRoute = wildflyService.createRoute();

        String wildflyHost = wildflyRoute.getRouteHost();
        Assertions.assertThat(wildflyHost).isEqualTo("wildfly-service.project.openshiftdomain");
    }

    @Test
    public void testRetrieveRouteHostNotExistingRoute() {
        Route wildflyRoute = new RouteImpl(controller.getClient(), projectName, "not-existing-service");
        Throwable thrown = Assertions.catchThrowable(() -> wildflyRoute.getRouteHost());

        Assertions.assertThat(thrown).isInstanceOf(RuntimeException.class)
                          .hasMessageContaining("Route for service 'not-existing-service' not found.");
    }
}

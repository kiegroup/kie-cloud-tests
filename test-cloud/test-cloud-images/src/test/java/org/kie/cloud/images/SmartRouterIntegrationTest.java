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

package org.kie.cloud.images;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.cloud.images.util.ImageUtils;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.image.ImageStream;
import org.kie.cloud.openshift.resource.DeploymentConfig;
import org.kie.cloud.openshift.resource.Pod;

public class SmartRouterIntegrationTest extends ImageTestBase {

    @Test
    public void testRouterHostAndPort() {
        String customRouterHost = "0.0.0.0";
        String customRouterPort = "1234";
        String expectedLog = "KieServerRouter started on " + customRouterHost + ":" + customRouterPort;

        Map<String, String> envVariables = new HashMap<String, String>();
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_HOST, customRouterHost);
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_ROUTER_PORT, customRouterPort);

        DeploymentConfig deploymentConfig = ImageUtils.deployImage(project, ImageStream.SMART_ROUTER, envVariables);

        try {
            List<Pod> pods = deploymentConfig.getPods();
            assertThat(pods).hasSize(1);
            assertThat(pods.get(0).getLog()).contains(expectedLog);
        } finally {
            deploymentConfig.scalePods(0);
            deploymentConfig.waitUntilAllPodsAreReady();
        }
    }
}

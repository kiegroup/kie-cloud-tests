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
package org.kie.cloud.openshift.util;

import java.net.MalformedURLException;
import java.net.URL;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.KubernetesList;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;

public class AmqImageStreamDeployer {

    public static void deploy(Project project) {
        try {
            OpenShift openShift = project.getOpenShiftAdmin();
            KubernetesList resourceList = openShift.lists()
                    .inNamespace("openshift")
                    .load(new URL(OpenShiftConstants.getAmqImageStreams()))
                    .get();
            resourceList.getItems().forEach(item -> {
                openShift.imageStreams()
                        .inNamespace("openshift")
                        .withName(item.getMetadata().getName())
                        .delete();
            });
            project.runOcCommandAsAdmin("apply", "-f", OpenShiftConstants.getAmqImageStreams(), "-n", "openshift");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        }
    }
}

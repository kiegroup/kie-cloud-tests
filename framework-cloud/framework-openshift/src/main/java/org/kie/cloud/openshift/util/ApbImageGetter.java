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

import org.kie.cloud.openshift.constants.OpenShiftConstants;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;

public class ApbImageGetter {

    private static OpenShiftClient openShiftClient;

    static {
        OpenShiftConfig openShiftConfig = new OpenShiftConfigBuilder()
                .withDisableApiGroupCheck(true)
                .build();
        openShiftClient = new DefaultOpenShiftClient(openShiftConfig);
    }

    /**
     * OpenShift APB image stream name is set by property "apb.image.stream.name".
     * Returns docker image name of apb image in namespece "openshift".
     * 
     * @return Name of docker image in OpenShift registry.
     */
    public static String fromImageStream() {
        return openShiftClient.imageStreams()
                .inNamespace("openshift")
                .withName(OpenShiftConstants.getApbImageStreamName())
                .get()
                .getStatus()
                .getDockerImageRepository();
    }
}

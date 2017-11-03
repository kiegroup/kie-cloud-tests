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

package org.kie.cloud.images.util;

import java.util.Map;

import org.kie.cloud.openshift.image.ImageStream;
import org.kie.cloud.openshift.resource.DeploymentConfig;
import org.kie.cloud.openshift.resource.Image;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.Service;

public class ImageUtils {

    public static DeploymentConfig deployImage(Project project, ImageStream imageStream, Map<String, String> envVariables) {
        Image image = project.getImage(imageStream);
        Service service = project.createService("image-test-service");

        DeploymentConfig deploymentConfig = service.createDeploymentConfig(image, envVariables);
        deploymentConfig.waitUntilAllPodsAreReady();

        return deploymentConfig;
    }
}

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

package org.kie.cloud.openshift.resource.impl;

import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.client.OpenShiftClient;
import org.kie.cloud.openshift.image.ImageStream;
import org.kie.cloud.openshift.resource.Image;

public class ImageImpl implements Image {

    private OpenShiftClient client;
    private String projectName;
    private ImageStream imageStream;

    public ImageImpl(OpenShiftClient client, String projectName, ImageStream imageStream) {
        this.client = client;
        this.projectName = projectName;
        this.imageStream = imageStream;
    }

    @Override
    public String getImageReference() {
        ImageStreamTag imageStreamTag = client.imageStreamTags().inNamespace(projectName).withName(imageStream.getImageStreamName()).get();
        return imageStreamTag.getImage().getDockerImageReference();
    }
}

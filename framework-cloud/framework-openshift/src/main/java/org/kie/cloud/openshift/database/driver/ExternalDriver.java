/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.database.driver;

import java.io.File;
import java.net.URL;

/**
 * Represents driver used by external database. This driver needs to be incorporated into Kie server image.
 */
public interface ExternalDriver {

    /**
     * @return Name of the driver. Used by Cekit to identify image to be built.
     */
    String getName();

    /**
     * @return Name of the driver image.
     */
    String getImageName();

    /**
     * @return Image version.
     */
    String getImageVersion();

    /**
     * @param dockerUrl URL to Docker registry.
     * @return Docker tag where the driver image is built using Cekit.
     */
    String getSourceDockerTag();

    /**
     * @param dockerUrl URL to Docker registry.
     * @return Docker tag where the driver image should be pushed to.
     */
    String getTargetDockerTag(URL dockerUrl);

    /**
     * @return Cekit command to build driver image.
     */
    String getCekitImageBuildCommand();
}

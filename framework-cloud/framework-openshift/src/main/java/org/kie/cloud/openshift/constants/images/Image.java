/*
 * Copyright 2018 JBoss by Red Hat.
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

package org.kie.cloud.openshift.constants.images;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.slf4j.LoggerFactory;

/**
 * OpenShift image
 */
public enum Image {
    AMQ(OpenShiftConstants.KIE_IMAGE_TAG_AMQ),
    CONSOLE(OpenShiftConstants.KIE_IMAGE_TAG_CONSOLE),
    CONTROLLER(OpenShiftConstants.KIE_IMAGE_TAG_CONTROLLER),
    KIE_SERVER(OpenShiftConstants.KIE_IMAGE_TAG_KIE_SERVER),
    MYSQL(OpenShiftConstants.KIE_IMAGE_TAG_MYSQL),
    POSTGRESQL(OpenShiftConstants.KIE_IMAGE_TAG_POSTGRESQL),
    SMARTROUTER(OpenShiftConstants.KIE_IMAGE_TAG_SMARTROUTER),
    WORKBENCH(OpenShiftConstants.KIE_IMAGE_TAG_WORKBENCH),
    WORKBENCH_INDEXING(OpenShiftConstants.KIE_IMAGE_TAG_WORKBENCH_INDEXING);

    private String systemPropertyForImageTag;
    private String imageGroup;
    private String imageName;
    private String imageRegistry;
    private String imageVersion;
    
    private String tag;

    private Image(String systemPropertyForImageTag) {
        this.systemPropertyForImageTag = systemPropertyForImageTag;
        Pattern imageTagPattern = Pattern.compile("^(?<registry>[a-zA-Z0-9-\\.:]*)/(?<group>[a-zA-Z0-9-]*)/(?<name>[a-zA-Z0-9-]*):?(?<version>[0-9\\\\.]*)-?([0-9\\.]*)$");
        tag = System.getProperty(systemPropertyForImageTag);

        if (tag == null || tag.isEmpty() ) {
            LoggerFactory.getLogger(Image.class).warn("System property for image tag {} is not defined. RuntimeException can be thrown later.", systemPropertyForImageTag);
        } else {
            parseImageTag(imageTagPattern, tag, systemPropertyForImageTag);
        }
    }

    public String getImageGroup() {
        return imageGroup;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageRegistry() {
        return imageRegistry;
    }

    public String getImageVersion() {
        return imageVersion;
    }

    public String getTag() {
        return tag;
    }

    public String getSystemPropertyForImageTag() {
        return systemPropertyForImageTag;
    }

    private void parseImageTag(Pattern imageTagPattern, String imageTag, String systemPropertyForImageTag) {
        Matcher matcher = imageTagPattern.matcher(imageTag);
        if (matcher.find()) {
            imageRegistry = matcher.group("registry");
            imageGroup = matcher.group("group");
            imageName = matcher.group("name");
            imageVersion = matcher.group("version");
        } else {
            throw new RuntimeException("System property for image tag '" + systemPropertyForImageTag + "' with value '" + imageTag + "' doesn't match expected pattern '" + imageTagPattern.pattern() + "'.");
        }
    }
}

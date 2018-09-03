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

/**
 * OpenShift image
 */
public enum Image {
    AMQ(OpenShiftConstants.KIE_IMAGE_TAG_AMQ, "registry.access.redhat.com/amq-broker-7-tech-preview/amq-broker-71-openshift:1.0"),
    CONSOLE(OpenShiftConstants.KIE_IMAGE_TAG_CONSOLE),
    CONTROLLER(OpenShiftConstants.KIE_IMAGE_TAG_CONTROLLER),
    KIE_SERVER(OpenShiftConstants.KIE_IMAGE_TAG_KIE_SERVER),
    MYSQL(OpenShiftConstants.KIE_IMAGE_TAG_MYSQL, "registry.access.redhat.com/rhscl/mysql-57-rhel7:5.7", "mysql", "5.7"),
    POSTGRESQL(OpenShiftConstants.KIE_IMAGE_TAG_POSTGRESQL, "registry.access.redhat.com/rhscl/postgresql-96-rhel7", "postgresql", "9.6"),
    SMARTROUTER(OpenShiftConstants.KIE_IMAGE_TAG_SMARTROUTER),
    WORKBENCH(OpenShiftConstants.KIE_IMAGE_TAG_WORKBENCH),
    WORKBENCH_INDEXING(OpenShiftConstants.KIE_IMAGE_TAG_WORKBENCH_INDEXING);

    private String imageGroup;
    private String imageName;
    private String imageRegistry;
    private String imageVersion;

    private String tag;

    private Image(String systemPropertyForImageTag) {
        this(systemPropertyForImageTag, null);
    }

    private Image(String systemPropertyForImageTag, String defaultImageTag) {
        this(systemPropertyForImageTag, defaultImageTag, null, null);
    }

    private Image(String systemPropertyForImageTag, String defaultImageTag, String imageStreamName, String imageStreamVersion) {
        Pattern imageTagPattern = Pattern.compile("^(?<registry>[a-zA-Z0-9-\\.:]*)/(?<group>[a-zA-Z0-9-]*)/(?<name>[a-zA-Z0-9-]*):?(?<version>[0-9\\\\.]*)-?([0-9\\.]*)$");
        tag = System.getProperty(systemPropertyForImageTag, defaultImageTag);

        if (tag == null || tag.isEmpty() ) {
            throw new RuntimeException("System property for image tag '" + systemPropertyForImageTag + "' is not defined.");
        } else {
            parseImageTag(imageTagPattern, tag, systemPropertyForImageTag, imageStreamName, imageStreamVersion);
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

    private void parseImageTag(Pattern imageTagPattern, String imageTag, String systemPropertyForImageTag, String imageStreamName, String imageStreamVersion) {
        Matcher matcher = imageTagPattern.matcher(imageTag);
        if (matcher.find()) {
            imageRegistry = matcher.group("registry");
            imageGroup = matcher.group("group");
            imageName = imageStreamName == null ? matcher.group("name") : imageStreamName;
            imageVersion = imageStreamVersion == null ? matcher.group("version") : imageStreamVersion;
        } else {
            throw new RuntimeException("System property for image tag '" + systemPropertyForImageTag + "' with value '" + imageTag + "' doesn't match expected pattern '" + imageTagPattern.pattern() + "'.");
        }
    }
}

/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * Cloud properties to configure some private settings. The property "cloud.properties.location" must point out to a file with
 * the constants defined in here. For example:
 *
 * ldap.docker.image=mydockerimage:latest
 */
public final class CloudProperties {

    private static final String CLOUD_PROPERTIES_LOCATION = "cloud.properties.location";
    private static final String LDAP_DOCKER_IMAGE_PROPERTY = "ldap.docker.image";
    private static final String GOGS_DOCKER_IMAGE_PROPERTY = "gogs.docker.image";

    private static CloudProperties INSTANCE;

    private final Properties prop;

    private CloudProperties() {
        String fileLocation = getFileLocation();
        try (InputStream is = new FileInputStream(new File(fileLocation))) {
            prop = new Properties();
            prop.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loarding cloud properties", e);
        }
    }

    public String getLdapDockerImage() {
        return getProperty(LDAP_DOCKER_IMAGE_PROPERTY);
    }

    public String getGogsDockerImage() {
        return getProperty(GOGS_DOCKER_IMAGE_PROPERTY);
    }

    private String getProperty(String key) {
        String value = prop.getProperty(key);
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException("Property '" + key + "' not found in " + getFileLocation());
        }

        return value;
    }

    private String getFileLocation() {
        return System.getProperty(CLOUD_PROPERTIES_LOCATION);
    }

    public static final synchronized CloudProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CloudProperties();
        }

        return INSTANCE;
    }
}

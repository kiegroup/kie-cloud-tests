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
import java.io.InputStream;
import java.util.Properties;

public final class CloudProperties {

    private static final String CLOUD_PROPERTIES_LOCATION = "cloud.properties.location";
    private static final String LDAP_DOCKER_IMAGE_PROPERTY = "ldap.docker.image";

    private static CloudProperties INSTANCE;

    private final Properties prop;

    private CloudProperties() {
        String fileLocation = System.getProperty(CLOUD_PROPERTIES_LOCATION);
        try (InputStream is = new FileInputStream(new File(fileLocation))) {
            prop = new Properties();
            prop.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Error loarding cloud properties", e);
        }
    }

    public String getLdapDockerImage() {
        return getProperty(LDAP_DOCKER_IMAGE_PROPERTY);
    }

    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    public synchronized static final CloudProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CloudProperties();
        }

        return INSTANCE;
    }
}

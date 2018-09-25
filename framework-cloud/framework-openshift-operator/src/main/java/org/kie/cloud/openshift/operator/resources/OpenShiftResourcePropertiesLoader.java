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

package org.kie.cloud.openshift.operator.resources;

import java.util.Properties;

import org.kie.cloud.openshift.util.PropertyLoader;

class OpenShiftResourcePropertiesLoader {

    private OpenShiftResourcePropertiesLoader() {
        // Util class
    }

    private static final Properties resourceUrlProperties = new Properties();

    static {
        loadResourcePropertiesFromResources();
    }

    static Properties getProperties() {
        return resourceUrlProperties;
    }

    private static void loadResourcePropertiesFromResources() {
        resourceUrlProperties.putAll(PropertyLoader.loadProperties(OpenShiftResourcePropertiesLoader.class, "resources.properties"));
    }
}

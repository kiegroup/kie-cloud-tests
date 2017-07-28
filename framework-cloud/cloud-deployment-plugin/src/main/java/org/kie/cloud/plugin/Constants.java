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

package org.kie.cloud.plugin;

public class Constants {

    public static final String
            PROPERTY_FILE_PATH = "cloud-urls.properties",
            CLOUD_API_IMPLEMENTATION_PROPERTY = "cloud.api.implementation",
            NAMESPACE_PROPERTY = "namespace",

            // properties required by UI tests to be able to connect to workbench via browser
            BUILD_PROPERTIES_WORKBENCH_IP = "as.ip",
            BUILD_PROPERTIES_WORKBENCH_PORT = "as.port",
            BUILD_PROPERTIES_WORKBENCH_CONTEXT_ROOT = "web.context-root",
            BUILD_PROPERTIES_WORKBENCH_USERNAME = "workbench.openshift.username",
            BUILD_PROPERTIES_WORKBENCH_PASSWORD = "workbench.openshift.password";
}

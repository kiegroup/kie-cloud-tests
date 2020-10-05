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

package org.kie.cloud.openshift.resource;

public class OpenShiftResourceConstants {

    // Project constants
    public static final long PROJECT_CREATION_TIMEOUT = 60 * 1000L; // 1 minute

    // Service constants
    public static final String EAP_DEFAULT_PROTOCOL = "TCP";
    public static final int EAP_DEFAULT_HTTP_PORT = 8080;

    // Deployment config constants
    public static final String DEPLOYMENT_TRIGGER_CONFIG_CHANGE = "ConfigChange";
    public static final long DEPLOYMENT_CONFIG_CREATION_TIMEOUT = 60 * 1000L; // 1 minute
    public static final String DEPLOYMENT_CONFIG_LABEL = "deploymentconfig";

    // Route constants
    public static final String ROUTE_REDIRECT_COMPONENT_TYPE = "Service";
    public static final int ROUTE_REDIRECT_DEFAULT_WEIGHT = 100;

    // Pod constants
    public static final long DEPLOYMENT_PODS_TERMINATION_TIMEOUT = 10 * 60 * 1000L; // 10 minutes
    public static final long DEPLOYMENT_NEW_VERSION_TIMEOUT = 10 * 60 * 1000L; // 10 minutes
    public static final long PODS_START_TO_READY_TIMEOUT = 10 * 60 * 1000L; // 10 minutes

    // Operator constants
    public static final long OPERATOR_START_TO_READY_TIMEOUT = 20 * 60 * 1000L; // 20 minutes
}

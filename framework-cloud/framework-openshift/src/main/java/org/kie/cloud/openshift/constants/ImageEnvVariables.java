/*
 * Copyright 2019 JBoss by Red Hat.
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
package org.kie.cloud.openshift.constants;

public class ImageEnvVariables {

    public static final String KIE_ADMIN_USER = "KIE_ADMIN_USER";
    public static final String KIE_ADMIN_PWD = "KIE_ADMIN_PWD";
    public static final String MAVEN_REPO_USERNAME = "MAVEN_REPO_USERNAME";

    public static final String KIE_SERVER_ID = "KIE_SERVER_ID";
    public static final String KIE_SERVER_MODE = "KIE_SERVER_MODE";
    public static final String KIE_SERVER_MEMORY_LIMIT = "KIE_SERVER_MEMORY_LIMIT";

    public static final String KIE_SERVER_ROUTER_ID = "KIE_SERVER_ROUTER_ID";

    public static final String EXTERNAL_MAVEN_REPO_URL = "EXTERNAL_MAVEN_REPO_URL";
    public static final String EXTERNAL_MAVEN_REPO_USERNAME = "EXTERNAL_MAVEN_REPO_USERNAME";
    public static final String EXTERNAL_MAVEN_REPO_PASSWORD = "EXTERNAL_MAVEN_REPO_PASSWORD";

    public static final String TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL = "TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL";

    public static final String HOSTNAME_HTTP = "HOSTNAME_HTTP";
    public static final String HOSTNAME_HTTPS = "HOSTNAME_HTTPS";

    public static final String RHDMCENTR_MAVEN_REPO_USERNAME = "RHDMCENTR_MAVEN_REPO_USERNAME";
    public static final String RHDMCENTR_MAVEN_REPO_PASSWORD = "RHDMCENTR_MAVEN_REPO_PASSWORD";
    public static final String RHPAMCENTR_MAVEN_REPO_USERNAME = "RHPAMCENTR_MAVEN_REPO_USERNAME";
    public static final String RHPAMCENTR_MAVEN_REPO_PASSWORD = "RHPAMCENTR_MAVEN_REPO_PASSWORD";

    public static final String GIT_HOOKS_DIR = "GIT_HOOKS_DIR";

    public static final String PROMETHEUS_SERVER_EXT_DISABLED = "PROMETHEUS_SERVER_EXT_DISABLED";

    public static final String DROOLS_SERVER_FILTER_CLASSES = "DROOLS_SERVER_FILTER_CLASSES";

    public static final String KIE_SERVER_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE = "KIE_SERVER_CONTROLLER_OPENSHIFT_PREFER_KIESERVER_SERVICE";

    public static final String ORG_APPFORMER_SIMPLIFIED_MONITORING_ENABLED = "ORG_APPFORMER_SIMPLIFIED_MONITORING_ENABLED";

    public static final String KIE_SERVER_CONTAINER_DEPLOYMENT = "KIE_SERVER_CONTAINER_DEPLOYMENT";
}

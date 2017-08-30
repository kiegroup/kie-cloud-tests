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

package org.kie.cloud.openshift.constants;

import java.util.Optional;

import org.kie.cloud.api.constants.Constants;

public class OpenShiftConstants implements Constants {

    public static final String OPENSHIFT_URL = "openshift.master.url";
    public static final String OPENSHIFT_USER = "openshift.username";
    public static final String OPENSHIFT_PASSWORD = "openshift.password";

    /**
     * Project name prefix - to simplify identification of projects created in OpenShift.
     */
    public static final String NAMESPACE_PREFIX = "openshift.namespace.prefix";
    /**
     * Used Kie application name. Needed for identification of services within the project.
     */
    public static final String KIE_APP_NAME = "kie.app.name";

    /**
     * URL pointing to OpenShift resource file containing keystore for HTTPS communication.
     */
    public static final String KIE_APP_SECRET = "kie.app.secret";
    /**
     * URL pointing to OpenShift resource file containing image streams with all available images.
     */
    public static final String KIE_IMAGE_STREAMS = "kie.image.streams";
    /**
     * URL pointing to OpenShift template file containing Workbench, Kie server and database.
     */
    public static final String KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER_DATABASE = "kie.app.template.workbench.kie-server.database";
    /**
     * URL pointing to OpenShift template file containing just Kie server.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER = "kie.app.template.kie-server";
    /**
     * URL pointing to OpenShift template file containing Kie server and database.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_DATABASE = "kie.app.template.kie-server.database";
    /**
     * URL pointing to OpenShift template file containing Workbench monitoring console and Smart router.
     */
    public static final String KIE_APP_TEMPLATE_CONSOLE_SMARTROUTER = "kie.app.template.workbench-monitoring.smartrouter";

    public static String getOpenShiftUrl() {
        return System.getProperty(OPENSHIFT_URL);
    }

    public static String getOpenShiftUserName() {
        return System.getProperty(OPENSHIFT_USER);
    }

    public static String getOpenShiftPassword() {
        return System.getProperty(OPENSHIFT_PASSWORD);
    }

    public static Optional<String> getNamespacePrefix() {
        return Optional.ofNullable(System.getProperty(NAMESPACE_PREFIX));
    }

    public static String getKieAppSecret() {
        return System.getProperty(KIE_APP_SECRET);
    }

    public static String getKieImageStreams() {
        return System.getProperty(KIE_IMAGE_STREAMS);
    }

    public static String getKieAppTemplateWorkbenchKieServerDatabase() {
        return System.getProperty(KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER_DATABASE);
    }

    public static String getKieAppTemplateKieServer() {
        return System.getProperty(KIE_APP_TEMPLATE_KIE_SERVER);
    }

    public static String getKieAppTemplateKieServerDatabase() {
        return System.getProperty(KIE_APP_TEMPLATE_KIE_SERVER_DATABASE);
    }

    public static String getKieAppTemplateConsoleSmartRouter() {
        return System.getProperty(KIE_APP_TEMPLATE_CONSOLE_SMARTROUTER);
    }

    public static String getKieApplicationName() {
        return System.getProperty(KIE_APP_NAME);
    }
}

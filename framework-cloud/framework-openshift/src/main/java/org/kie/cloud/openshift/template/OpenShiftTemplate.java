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

package org.kie.cloud.openshift.template;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.openshift.constants.OpenShiftConstants;

/**
 * OpenShift templates which are currently available.
 */
public enum OpenShiftTemplate {
    WORKBENCH_KIE_SERVER(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER),
    WORKBENCH_KIE_SERVER_PERSISTENT(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER_PERSISTENT),
    KIE_SERVER_DATABASE_EXTERNAL(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_DATABASE_EXTERNAL),
    KIE_SERVER(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER),
    KIE_SERVER_POSTGRESQL(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_POSTGRESQL),
    KIE_SERVER_MYSQL(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_MYSQL),
    CLUSTERED_CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES(OpenShiftConstants.CLUSTERED_CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES),
    CLUSTERED_WORKBENCH_KIE_SERVER_PERSISTENT(OpenShiftConstants.CLUSTERED_WORKBENCH_KIE_SERVER_PERSISTENT),
    CLUSTERED_WORKBENCH_KIE_SERVER_DATABASE_PERSISTENT(OpenShiftConstants.CLUSTERED_WORKBENCH_KIE_SERVER_DATABASE_PERSISTENT),
    CONSOLE(OpenShiftConstants.KIE_APP_TEMPLATE_CONSOLE),
    KIE_SERVER_HTTPS_S2I(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_HTTPS_S2I),
    WORKBENCH(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH),
    CONTROLLER(OpenShiftConstants.KIE_APP_TEMPLATE_CONTROLLER),
    SMARTROUTER(OpenShiftConstants.KIE_APP_TEMPLATE_SMARTROUTER),
    SSO(OpenShiftConstants.SSO_APP_TEMPLATE),
    // Special template containing secret file.
    SECRET(OpenShiftConstants.KIE_APP_SECRET),
    CUSTOM_TRUSTED_SECRET(OpenShiftConstants.CUSTOM_TRUSTED_APP_SECRET),
    SSO_SECRET(OpenShiftConstants.SSO_APP_SECRETS);

    private static final Properties templateProperties = OpenShiftTemplatePropertiesLoader.getProperties();

    private final String propertyKey;

    /**
     * @param propertyKey property key for URL pointing to template location.
     */
    OpenShiftTemplate(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * @return URL pointing to the template.
     * @throws MissingResourceException If template is missing or template URL is malformed.
     */
    public URL getTemplateUrl() throws MissingResourceException {
        // Allow override from command line
        String fromSystemProperty = System.getProperty(propertyKey);
        String fromResources = templateProperties.getProperty(propertyKey);
        String urlString = fromSystemProperty == null || fromSystemProperty.isEmpty() ? fromResources : fromSystemProperty;

        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new MissingResourceException("Invalid URL '" + urlString + "' specified by property " + propertyKey, e);
        }
    }
}

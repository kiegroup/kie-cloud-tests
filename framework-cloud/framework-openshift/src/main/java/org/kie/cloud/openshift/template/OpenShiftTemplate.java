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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES(OpenShiftConstants.CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES),
    CLUSTERED_WORKBENCH_KIE_SERVER_DATABASE_PERSISTENT(OpenShiftConstants.CLUSTERED_WORKBENCH_KIE_SERVER_DATABASE_PERSISTENT),
    CONSOLE(OpenShiftConstants.KIE_APP_TEMPLATE_CONSOLE),
    KIE_SERVER_HTTPS_S2I(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_HTTPS_S2I),
    WORKBENCH(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH),
    CONTROLLER(OpenShiftConstants.KIE_APP_TEMPLATE_CONTROLLER),
    SMARTROUTER(OpenShiftConstants.KIE_APP_TEMPLATE_SMARTROUTER),
    // Special template containing secret file.
    SECRET(OpenShiftConstants.KIE_APP_SECRET);

    private static final Properties templateProperties = filterOpenShiftTemplateProperties(OpenShiftTemplatePropertiesLoader.getProperties());

    private final String propertyKey;

    /**
     * @param propertyKey property key for URL pointing to template location.
     */
    OpenShiftTemplate(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * @return System property where template URL is stored.
     */
    private String getPropertyKey() {
        return propertyKey;
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

    /**
     * Process OpenShift template properties and replace any variable with appropriate value. The value can be retrieved from another property in template properties.
     *
     * @param openShiftTemplateProperties Properties to be processed.
     * @return Processed properties.
     */
    private static Properties filterOpenShiftTemplateProperties(Properties openShiftTemplateProperties) {
        Pattern replaceTagPattern = Pattern.compile(".*\\$\\{(.*)\\}.*");
        Properties processedProperties = new Properties();

        openShiftTemplateProperties.forEach((k,v) -> {
            String value = (String) v;
            Matcher matcher = replaceTagPattern.matcher(value);
            if(matcher.matches()) {
                // If property tag is provided using system properties then it is replaced automatically when loading properties by Java.
                // This step is used just in case the replacement is another property from loaded properties.
                String propertyValue = openShiftTemplateProperties.getProperty(matcher.group(1));
                processedProperties.put(k, value.replace("${" + matcher.group(1) + "}", propertyValue));
            } else {
                processedProperties.put(k, v);
            }
        });

        return processedProperties;
    }
}

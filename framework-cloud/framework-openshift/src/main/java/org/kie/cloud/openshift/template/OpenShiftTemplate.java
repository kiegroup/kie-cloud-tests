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

import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.openshift.constants.OpenShiftConstants;

/**
 * OpenShift templates which are currently available.
 */
public enum OpenShiftTemplate {
    WORKBENCH_KIE_SERVER(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER),
    WORKBENCH_KIE_SERVER_DATABASE(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER_DATABASE),
    KIE_SERVER_DATABASE_EXTERNAL(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_DATABASE_EXTERNAL),
    KIE_SERVER(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER),
    KIE_SERVER_DATABASE(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_DATABASE),
    CONSOLE_SMARTROUTER(OpenShiftConstants.KIE_APP_TEMPLATE_CONSOLE_SMARTROUTER),
    CONSOLE(OpenShiftConstants.KIE_APP_TEMPLATE_CONSOLE),
    KIE_SERVER_S2I(OpenShiftConstants.KIE_APP_TEMPLATE_KIE_SERVER_S2I),
    WORKBENCH(OpenShiftConstants.KIE_APP_TEMPLATE_WORKBENCH),
    SMARTROUTER(OpenShiftConstants.KIE_APP_TEMPLATE_SMARTROUTER);

    private String templateSystemPropertyKey;

    /**
     * @param templateSystemProperty System property key for URL pointing to template location.
     */
    private OpenShiftTemplate(String templateSystemPropertyKey) {
        this.templateSystemPropertyKey = templateSystemPropertyKey;
    }

    /**
     * @return System property where template URL is stored.
     */
    public String getTemplateSystemPropertyKey() {
        return templateSystemPropertyKey;
    }

    /**
     * @return URL pointing to the template.
     * @throws MissingResourceException If template is missing or template URL is malformed.
     */
    public URL getTemplateUrl() throws MissingResourceException {
        String templateSystemPropertyValue = System.getProperty(templateSystemPropertyKey);

        try {
            return new URL(templateSystemPropertyValue);
        } catch (MalformedURLException e) {
            throw new MissingResourceException("Template referenced by system property " + templateSystemPropertyKey + " with value '" + templateSystemPropertyValue + "' cannot be processed.", e);
        }
    }
}

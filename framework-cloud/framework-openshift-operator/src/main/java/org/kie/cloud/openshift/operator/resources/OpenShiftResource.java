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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.openshift.operator.constants.OpenShiftOperatorConstants;

/**
 * OpenShift resources which are currently available.
 */
public enum OpenShiftResource {
    WORKBENCH_KIE_SERVER(OpenShiftOperatorConstants.KIE_APP_OPERATOR_DEPLOYMENTS_WORKBENCH_KIE_SERVER),
    // Configuration resources.
    CRD(OpenShiftOperatorConstants.KIE_APP_OPERATOR_DEPLOYMENTS_CRD),
    RBAC(OpenShiftOperatorConstants.KIE_APP_OPERATOR_DEPLOYMENTS_RBAC),
    OPERATOR(OpenShiftOperatorConstants.KIE_APP_OPERATOR_DEPLOYMENTS_OPERATOR);

    private static final Properties resourceProperties = OpenShiftResourcePropertiesLoader.getProperties();

    private final String propertyKey;

    /**
     * @param propertyKey property key for URL pointing to resource location.
     */
    OpenShiftResource(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * @return URL pointing to the resource.
     * @throws MissingResourceException If resource is missing or resource URL is malformed.
     */
    public URL getResourceUrl() throws MissingResourceException {
        // Allow override from command line
        String fromSystemProperty = System.getProperty(propertyKey);
        String fromResources = resourceProperties.getProperty(propertyKey);
        String urlString = fromSystemProperty == null || fromSystemProperty.isEmpty() ? fromResources : fromSystemProperty;

        try {
            return new URL(urlString);
        } catch (MalformedURLException e) {
            throw new MissingResourceException("Invalid URL '" + urlString + "' specified by property " + propertyKey, e);
        }
    }
}

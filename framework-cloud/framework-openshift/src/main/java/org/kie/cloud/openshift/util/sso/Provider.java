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

package org.kie.cloud.openshift.util.sso;

public enum Provider {
    OIDC_JBOSS_XML_SUBSYSTEM("keycloak-oidc-jboss-subsystem", "Keycloak OIDC JBoss Subsystem XML"),
    SAML_JBOSS_XML_SUBSYSTEM("keycloak-saml-subsystem", "Keycloak SAML Wildfly/JBoss Subsystem"),
    OIDC_KEYCLOAK_JSON("keycloak-oidc-keycloak-json", "Keycloak OIDC JSON");

    private final String providerId;
    private final String webUiLabel;

    private Provider(String providerId, String webUiLabel) {
        this.providerId = providerId;
        this.webUiLabel = webUiLabel;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getWebUiLabel() {
        return webUiLabel;
    }
}

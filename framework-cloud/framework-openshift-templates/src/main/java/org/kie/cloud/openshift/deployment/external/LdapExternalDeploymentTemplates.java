/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.LdapDeployment;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.external.impl.AbstractLdapExternalDeployment;

public class LdapExternalDeploymentTemplates extends AbstractLdapExternalDeployment<Map<String, String>> implements ExternalDeploymentTemplates<LdapDeployment> {

    public LdapExternalDeploymentTemplates(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(Map<String, String> extraVars) {
        LdapDeployment deployment = getDeploymentInformation();
        addEnvVar(extraVars, OpenShiftTemplateConstants.AUTH_LDAP_URL, deployment.getHost());
    }

    private void addEnvVar(Map<String, String> extraVars, String key, String value) {
        extraVars.put(key, value);
    }
}

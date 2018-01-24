/*
 * Copyright 2018 JBoss by Red Hat.
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
package org.kie.cloud.openshift.settings.builder;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.settings.DeploymentSettings;
import org.kie.cloud.api.settings.builder.KieServerS2ISettingsBuilder;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.settings.DeploymentSettingsImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;

public class KieServerBasicS2ISettingsBuilderImpl extends KieServerS2ISettingsBuilderImpl {

    private final OpenShiftTemplate appTemplate = OpenShiftTemplate.KIE_SERVER_BASIC_S2I;

    public KieServerBasicS2ISettingsBuilderImpl() {
        super();

        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());
    }

    @Override
    public DeploymentSettings build() {
        return new DeploymentSettingsImpl(envVariables, appTemplate);
    }

    @Override
    public KieServerS2ISettingsBuilder withSecuredHostame(String https) {
        throw new RuntimeException("Secured hostname can't be configured for the template without https.");
    }
}

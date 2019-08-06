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

package org.kie.cloud.openshift.scenario;

import java.util.Map;

import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeploymentTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KieCommonScenario<T extends DeploymentScenario<T>> extends OpenShiftScenario<T> {

    private static final Logger logger = LoggerFactory.getLogger(KieCommonScenario.class);

    protected Map<String, String> envVariables;

    public KieCommonScenario(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void configureWithExternalDeployment(ExternalDeployment<?, ?> externalDeployment) {
        logger.info("configureWithExternalDeployment {}", externalDeployment.getKey());
        ((ExternalDeploymentTemplates) externalDeployment).configure(envVariables);
    }

}

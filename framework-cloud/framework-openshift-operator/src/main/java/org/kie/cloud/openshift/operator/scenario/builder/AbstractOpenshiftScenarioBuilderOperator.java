/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.operator.scenario.builder;

import org.kie.cloud.openshift.deployment.external.ExternalDeploymentFactory;
import org.kie.cloud.openshift.operator.deployment.external.ExternalDeploymentFactoryOperator;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.scenario.builder.AbstractOpenshiftScenarioBuilder;

public abstract class AbstractOpenshiftScenarioBuilderOperator<T> extends AbstractOpenshiftScenarioBuilder<T> {

    private ExternalDeploymentFactory factory = new ExternalDeploymentFactoryOperator();

    @Override
    protected ExternalDeploymentFactory getExternalDeploymentFactory() {
        return factory;
    }

    private boolean isDisableSslNull(KieApp kieApp) {
        return kieApp.getSpec().getCommonConfig().getDisableSsl() == null;
    }

    private boolean isDisableSslTrue(KieApp kieApp) {
        return kieApp.getSpec().getCommonConfig().getDisableSsl().booleanValue();
    }

    private boolean isDisableSslFalse(KieApp kieApp) {
        return !isDisableSslTrue(kieApp);
    }

    protected void checkHttpKieServerRouteConfig(boolean kieServerHostnameSet, KieApp kieApp) {
        if (kieServerHostnameSet) {
            throw new IllegalStateException("Kie Server hostname has been already set. Update your scenario.");
        }
        if (!isDisableSslNull(kieApp) && isDisableSslFalse(kieApp)) {
            throw new IllegalStateException("SSL is enabled. It is not possible to configure http hostname.");
        }

        if (isDisableSslNull(kieApp)) {
            kieApp.getSpec().getCommonConfig().setDisableSsl(true);
        }

    }

    protected void checkHttpsKieServerRouteConfig(boolean kieServerHostnameSet, KieApp kieApp) {
        if (kieServerHostnameSet) {
            throw new IllegalStateException("Kie Server hostname has been already set. Update your scenario.");
        }
        if (!isDisableSslNull(kieApp) && isDisableSslTrue(kieApp)) {
            throw new IllegalStateException("SSL is disabled. It is not possible to configure https hostname.");
        }

        if (isDisableSslNull(kieApp)) {
            kieApp.getSpec().getCommonConfig().setDisableSsl(false);
        }
    }


    protected void checkHttpWorkbenchRouteConfig(boolean workbenchHostnameSet, KieApp kieApp) {
        if (workbenchHostnameSet) {
            throw new IllegalStateException("Workbench hostname has been already set. Update your scenario.");
        }
        if (!isDisableSslNull(kieApp) && isDisableSslFalse(kieApp)) {
            throw new IllegalStateException("SSL is enabled. It is not possible to configure http hostname.");
        }

        if (isDisableSslNull(kieApp)) {
            kieApp.getSpec().getCommonConfig().setDisableSsl(true);
        }
    }

    protected void checkHttpsWorkbenchRouteConfig(boolean workbenchHostnameSet, KieApp kieApp) {
        if (workbenchHostnameSet) {
            throw new IllegalStateException("Workbench hostname has been already set. Update your scenario.");
        }
        if (!isDisableSslNull(kieApp) && isDisableSslTrue(kieApp)) {
            throw new IllegalStateException("SSL is disabled. It is not possible to configure https hostname.");
        }

        if (isDisableSslNull(kieApp)) {
            kieApp.getSpec().getCommonConfig().setDisableSsl(false);
        }
    }
}

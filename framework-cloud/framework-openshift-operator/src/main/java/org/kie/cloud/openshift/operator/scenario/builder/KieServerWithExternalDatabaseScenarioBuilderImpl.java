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
package org.kie.cloud.openshift.operator.scenario.builder;

import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment.ExternalDeploymentID;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;
import org.kie.cloud.openshift.operator.scenario.KieServerWithExternalDatabaseScenarioImpl;

public class KieServerWithExternalDatabaseScenarioBuilderImpl extends AbstractTrialOpenshiftScenarioBuilderOperator<KieServerWithExternalDatabaseScenario> implements KieServerWithExternalDatabaseScenarioBuilder {

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder withInternalMavenRepo(boolean waitForRunning) {
        setAsyncExternalDeployment(ExternalDeploymentID.MAVEN_REPOSITORY);
        return this;
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder withKieServerId(String kieServerId) {
        for (Server server : getKieApp().getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.KIE_SERVER_ID, kieServerId));
        }

        return this;
    }

    @Override
    protected KieServerWithExternalDatabaseScenario getDeploymentScenarioInstance() {
        return new KieServerWithExternalDatabaseScenarioImpl(getKieApp());
    }

}

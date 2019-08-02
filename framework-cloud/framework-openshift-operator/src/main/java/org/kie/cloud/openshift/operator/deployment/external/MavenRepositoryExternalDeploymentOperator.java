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
package org.kie.cloud.openshift.operator.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.constants.ImageEnvVariables;
import org.kie.cloud.openshift.deployment.external.impl.AbstractMavenRepositoryExternalDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;
import org.kie.cloud.openshift.operator.model.components.Env;
import org.kie.cloud.openshift.operator.model.components.Server;

public class MavenRepositoryExternalDeploymentOperator extends AbstractMavenRepositoryExternalDeployment<KieApp> implements ExternalDeploymentOperator<MavenRepositoryDeployment> {

    public MavenRepositoryExternalDeploymentOperator(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(KieApp kieApp) {
        super.configure(kieApp);

        MavenRepositoryDeployment deployment = getDeploymentInformation();
        for (Server server : kieApp.getSpec().getObjects().getServers()) {
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString()));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_USERNAME, deployment.getUsername()));
            server.addEnv(new Env(ImageEnvVariables.EXTERNAL_MAVEN_REPO_PASSWORD, deployment.getPassword()));
        }
    }

}

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
package org.kie.cloud.api.deployment;

import java.util.Map;

import org.kie.cloud.maven.MavenDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;

public class KjarDeployer {

    private static final String KJAR_SOURCES_FOLDER = "/kjars-sources/";

    private Kjar kjar;

    private KjarDeployer(Kjar kjar) {
        this.kjar = kjar;
    }

    public static final KjarDeployer create(Kjar kjar) {
        return new KjarDeployer(kjar);
    }

    public void deploy(MavenRepositoryDeployment repositoryDeployment) {
        // Synchronize on kjar deployment to avoid conflicts
        synchronized (kjar) {
            MavenDeployer.buildAndDeployMavenProject(KjarDeployer.class.getResource(KJAR_SOURCES_FOLDER + kjar.getProjectName()).getFile(), repositoryDeployment);
        }
    }
}

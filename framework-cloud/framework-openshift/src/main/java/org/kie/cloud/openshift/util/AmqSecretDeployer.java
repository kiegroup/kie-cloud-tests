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
package org.kie.cloud.openshift.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import org.apache.commons.codec.binary.Base64;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.openshift.resource.Project;

public class AmqSecretDeployer {

    private AmqSecretDeployer() {}

    public static void create(Project project) {
        try {
            Secret amqAppSecret = new SecretBuilder()
                    .withNewMetadata()
                        .withName("amq-app-secret")
                        .withNamespace(project.getName())
                        .endMetadata()
                    .addToData("broker.ks", 
                            Base64.encodeBase64String(Files.readAllBytes(Paths.get(DeploymentConstants.getCertificateDir()+"/broker.ks")))) //TODO replace with constants
                    .addToData("broker.ts", 
                            Base64.encodeBase64String(Files.readAllBytes(Paths.get(DeploymentConstants.getCertificateDir()+"/broker.ts")))) //TODO replace with constants
                    .build();
            project.getOpenShift().secrets().createOrReplace(amqAppSecret);
        } catch (IOException ex) {
            throw new RuntimeException("Exception cat during creating AMQ secret." , ex);
        }
    }
}

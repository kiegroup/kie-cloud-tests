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

package org.kie.cloud.common.provider;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.kie.cloud.api.deployment.ControllerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.KieServerControllerClientFactory;

public class KieServerControllerClientProvider {

    public static KieServerControllerClient getKieServerControllerClient(WorkbenchDeployment workbenchDeployment) {
        return KieServerControllerClientFactory.newRestClient(workbenchDeployment.getUrl().toString() + "/rest/controller",
                workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
    }

    public static KieServerControllerClient getKieServerControllerClient(ControllerDeployment controllerDeployment) {
        return KieServerControllerClientFactory.newRestClient(controllerDeployment.getUrl().toString() + "/rest/controller",
                controllerDeployment.getUsername(), controllerDeployment.getPassword());
    }

    /**
     * Wait until server templates are created in controller.
     */
    public static void waitForServerTemplateCreation(WorkbenchDeployment workbenchDeployment, int numberOfServerTemplates) {
        Instant timeoutTime = Instant.now().plusSeconds(300);
        while (Instant.now().isBefore(timeoutTime)) {

            ServerTemplateList serverTemplates = getKieServerControllerClient(workbenchDeployment).listServerTemplates();
            if(serverTemplates.getServerTemplates() != null && serverTemplates.getServerTemplates().length == numberOfServerTemplates) {
                return;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server template creation.", e);
            }
        }
        ServerTemplateList serverTemplates = getKieServerControllerClient(workbenchDeployment).listServerTemplates();
        String templates = Arrays.stream(serverTemplates.getServerTemplates()).map(ServerTemplateKey::getId).collect(Collectors.joining(", "));
        throw new RuntimeException("Timeout while waiting for 300 seconds for server template creation. Expected " + numberOfServerTemplates + " templates (" + serverTemplates.getServerTemplates()
                + "), but got these templates: " + templates);
    }
}


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
import java.util.Collection;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;

public class KieServerControllerClientProvider {

    private KieServerMgmtControllerClient kieServerMgmtControllerClient;

    public KieServerControllerClientProvider(WorkbenchDeployment workbenchDeployment) {
        kieServerMgmtControllerClient = new KieServerMgmtControllerClient(workbenchDeployment.getUrl().toString() + "/rest/controller",
                workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
    }

    public KieServerMgmtControllerClient getKieServerMgmtControllerClient() {
        return kieServerMgmtControllerClient;
    }

    /**
     * Wait until any server template is created in controller.
     */
    public void waitForServerTemplateCreation() {
        Instant timeoutTime = Instant.now().plusSeconds(30);
        while (Instant.now().isBefore(timeoutTime)) {

            Collection<ServerTemplate> serverTemplates = kieServerMgmtControllerClient.listServerTemplates();
            if(!serverTemplates.isEmpty()) {
                return;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server template creation.", e);
            }
        }
        throw new RuntimeException("Timeout while waiting for 30 seconds for server template creation.");
    }
}


/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.workbenchha.runners;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public abstract class AbstractRunner {

    protected KieServerControllerClient kieControllerClient;
    protected KieServicesClient kieServerClient;
    protected WorkbenchClient workbenchClient;
    protected WorkbenchClient asyncWorkbenchClient;
    protected String wbUser;

    public AbstractRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(workbenchDeployment, user, password);
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(workbenchDeployment, user, password);
        asyncWorkbenchClient = WorkbenchClientProvider.getAsyncWorkbenchClient(workbenchDeployment, user, password);
        wbUser = workbenchDeployment.getUsername();
    }
    
}
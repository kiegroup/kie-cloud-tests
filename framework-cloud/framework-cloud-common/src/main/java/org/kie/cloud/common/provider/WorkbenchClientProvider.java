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

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.wb.test.rest.client.RestWorkbenchClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class WorkbenchClientProvider {

    public static WorkbenchClient getWorkbenchClient(WorkbenchDeployment workbenchDeployment) {
        WorkbenchClient workbenchClient = RestWorkbenchClient.createWorkbenchClient(workbenchDeployment.getUrl().orElseGet(workbenchDeployment.getSecureUrl()::get).toString(),
                workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
        return workbenchClient;
    }
}


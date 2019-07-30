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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.guvnor.rest.client.CloneProjectRequest;
import org.kie.cloud.api.deployment.WorkbenchDeployment;

public class ImportRunner extends AbstractRunner {

    public ImportRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        super(workbenchDeployment, user, password);
        // TODO Auto-generated constructor stub
    }

    public Callable<Collection<String>> importProjects(String spaceName, String projectName, int startSuffix,
            int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                List<String> importedProjects = new ArrayList<>(retries);
                for (int i = startSuffix; i < startSuffix + retries; i++) {
                    final CloneProjectRequest cloneProjectRequest = new CloneProjectRequest();
                    cloneProjectRequest.setName(projectName + "-" + i);
                    cloneProjectRequest.setGitURL(""); // TODO add some resource

                    workbenchClient.cloneRepository(spaceName, cloneProjectRequest);

                    importedProjects.add(projectName + "-" + i);
                }
                return importedProjects;
            }
        };
    }
}
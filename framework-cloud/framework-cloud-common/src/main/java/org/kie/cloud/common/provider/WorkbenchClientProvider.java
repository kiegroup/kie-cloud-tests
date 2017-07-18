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

import org.guvnor.rest.client.OrganizationalUnit;
import org.guvnor.rest.client.RepositoryRequest;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.wb.test.rest.client.RestWorkbenchClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class WorkbenchClientProvider {

    private WorkbenchClient workbenchClient;

    public WorkbenchClientProvider(WorkbenchDeployment workbenchDeployment) {
        workbenchClient = RestWorkbenchClient.createWorkbenchClient(workbenchDeployment.getUrl().toString(),
                workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
    }

    public void createOrganizationalUnit(String orgUnitName, String owner) {
        OrganizationalUnit orgUnit = new OrganizationalUnit();
        orgUnit.setName(orgUnitName);
        orgUnit.setOwner(owner);
        workbenchClient.createOrganizationalUnit(orgUnit);
    }

    public void cloneRepository(String orgUnitName, String repoName, String gitUrl) {
        RepositoryRequest repository = new RepositoryRequest();
        repository.setName(repoName);
        repository.setOrganizationalUnitName(orgUnitName);
        repository.setGitURL(gitUrl);
        repository.setRequestType("clone");
        workbenchClient.createOrCloneRepository(repository);
    }

    public void deployProject(String repoName, String projectName) {
        workbenchClient.deployProject(repoName, projectName);
    }
}


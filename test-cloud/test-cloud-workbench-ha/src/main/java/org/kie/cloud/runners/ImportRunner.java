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

package org.kie.cloud.runners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.guvnor.rest.client.CloneProjectRequest;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.tests.common.provider.git.Git;
import org.kie.cloud.util.SpaceProjects;
import org.kie.wb.test.rest.client.WorkbenchClient;

/**
 * Runner for work with Project importing
 */
public class ImportRunner extends AbstractRunner {

    private Collection<SpaceProjects> allCreatedSpaceProjects;

    public ImportRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        super(workbenchDeployment, user, password);
        allCreatedSpaceProjects = new ArrayList<>();
    }

    public Callable<SpaceProjects> asyncImportProjects(String spaceName, String gitURL, String projectName) {
        return new Callable<SpaceProjects>() {
            @Override
            public SpaceProjects call() {
                return createSpaceAndImportProject(asyncWorkbenchClient, spaceName, gitURL, projectName);
            }
        };
    }

    public Callable<SpaceProjects> asyncImportProjects(String spaceName, Map<String, String> projectNameAndGitRepoName) {
        return new Callable<SpaceProjects>() {
            @Override
            public SpaceProjects call() {
                return createSpaceAndImportProject(asyncWorkbenchClient,spaceName,projectNameAndGitRepoName);
            }
        };
    }

    public Callable<SpaceProjects> importProjects(String spaceName, Map<String, String> projectNameAndGitRepoName) {
        return new Callable<SpaceProjects>() {
            @Override
            public SpaceProjects call() {
                return createSpaceAndImportProject(workbenchClient,spaceName,projectNameAndGitRepoName);
            }
        };
    }

    // TODO can I delete this?
    public Callable<SpaceProjects> importProjects(String spaceName, String gitURL, String projectNames) {
        return new Callable<SpaceProjects>() {
            @Override
            public SpaceProjects call() {
                return createSpaceAndImportProject(workbenchClient, spaceName, gitURL, projectNames);
            }
        };
    }

    private SpaceProjects createSpaceAndImportProject(WorkbenchClient client, String spaceName, Map<String, String> projectNameAndGitRepoName) {
        client.createSpace(spaceName, wbUser);
        projectNameAndGitRepoName.forEach((String projectName, String gitRepoName) -> {
            importProject(client, spaceName, Git.getProvider().getRepositoryUrl(gitRepoName), projectName);
        });
        SpaceProjects spaceProjects = new SpaceProjects(spaceName, projectNameAndGitRepoName.keySet());
        allCreatedSpaceProjects.add(spaceProjects);
        return spaceProjects;
    }

    private SpaceProjects createSpaceAndImportProject(WorkbenchClient client, String spaceName, String gitURL, String projectName) {
        client.createSpace(spaceName, wbUser);
        importProject(client, spaceName, gitURL, projectName);
        SpaceProjects spaceProjects = new SpaceProjects(spaceName, projectName);
        allCreatedSpaceProjects.add(spaceProjects);
        return spaceProjects;
    }

    private void importProject(WorkbenchClient client, String spaceName, String gitURL, String name) {
        final CloneProjectRequest cloneProjectRequest = new CloneProjectRequest();
        cloneProjectRequest.setName(name);
        cloneProjectRequest.setGitURL(gitURL);

        client.cloneRepository(spaceName, cloneProjectRequest);
    }
}
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
import java.util.List;
import java.util.concurrent.Callable;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.util.SpaceProjects;
import org.kie.wb.test.rest.client.WorkbenchClient;

/**
 * Runner for work with Space and Projects
 */
public class SpaceProjectRunner extends AbstractRunner {

    private Collection<SpaceProjects> allCreatedSpaceProjects;

    public SpaceProjectRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        super(workbenchDeployment, user, password);
        allCreatedSpaceProjects = new ArrayList<>();
    }

    private static final String GROUP_ID = "org.kie.cloud.testing";
    private static final String VERSION = "1.0.0";
    

    public Callable<SpaceProjects> createSpaceAndProject(String newSpaceName, String newProjectName) {
        return new Callable<SpaceProjects>() {
            @Override
            public SpaceProjects call() {
                return createSpaceAndProjects(workbenchClient, newSpaceName, newProjectName, 0, 1);
            }
        };
    }

    private List<String> createProjects(WorkbenchClient client, String spaceName, String projectName, int startSuffix, int retries) {
        List<String> createdProjects = new ArrayList<>(retries);
        for (int i = startSuffix; i < startSuffix + retries; i++) {
            client.createProject(spaceName, projectName + "-" + i, GROUP_ID, VERSION);
            createdProjects.add(projectName + "-" + i);
        }
        allCreatedSpaceProjects.add(new SpaceProjects(spaceName, createdProjects));
        return createdProjects;
    }

    private SpaceProjects createSpaceAndProjects(WorkbenchClient client, String spaceName, String projectName, int startSuffix, int retries) {
        client.createSpace(spaceName, wbUser);
        return new SpaceProjects(spaceName, createProjects(client, spaceName, projectName, startSuffix, retries));
    }

    /*
    public Callable<Collection<ProjectResponse>> getProjects(String spaceName) {
        return new Callable<Collection<ProjectResponse>>() {
            @Override
            public Collection<ProjectResponse> call() {
                return workbenchClient.getProjects(spaceName);
            }
        };
    } */

    public Callable<Void> deleteProjects() {
        return new Callable<Void>() {
            @Override
            public Void call() {
                allCreatedSpaceProjects.forEach(spaceProject->{
                    spaceProject.getProjectNames().forEach(name-> {
                        workbenchClient.deleteProject(spaceProject.getSpaceName(), name);
                    });
                });
                return null;
            }
        };
    }

}
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

import org.guvnor.rest.client.ProjectResponse;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.util.SpaceProjects;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class ProjectRunner extends AbstractRunner {

    private Collection<String> allCreatedProjects;

    public ProjectRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        super(workbenchDeployment, user, password);
        allCreatedProjects = new ArrayList<>();
    }

    private static final String GROUP_ID = "org.kie.cloud.testing";
    private static final String VERSION = "1.0.0";
    
    public Callable<Collection<String>> createProject(String spaceName, String projectName) {
        return createProjects(spaceName, projectName, 0, 1);
    }

    public Callable<SpaceProjects> createSpaceAndProject(String newSpaceName, String newProjectName) {
        return new Callable<SpaceProjects>() {
            @Override
            public SpaceProjects call() {
                return createSpaceAndProjects(workbenchClient, newSpaceName, newProjectName, 0, 1);
            }
        };
    }

    public Callable<Collection<String>> createProjects(String spaceName, String projectName, int startSuffix, int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                return createProjects(workbenchClient, spaceName, projectName, startSuffix, retries);
            }
        };
    }

    public Callable<Collection<String>> asyncCreateProjects(String spaceName, String projectName, int startSuffix, int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                return createProjects(asyncWorkbenchClient, spaceName, projectName, startSuffix, retries);
            }
        };
    }

    private List<String> createProjects(WorkbenchClient client, String spaceName, String projectName, int startSuffix, int retries) {
        List<String> createdProjects = new ArrayList<>(retries);
        for (int i = startSuffix; i < startSuffix + retries; i++) {
            client.createProject(spaceName, projectName + "-" + i, GROUP_ID, VERSION);
            createdProjects.add(projectName + "-" + i);
        }
        allCreatedProjects.addAll(createdProjects);
        return createdProjects;
    }

    private SpaceProjects createSpaceAndProjects(WorkbenchClient client, String spaceName, String projectName, int startSuffix, int retries) {
        client.createSpace(spaceName, wbUser);
        return new SpaceProjects(spaceName, createProjects(client, spaceName, projectName, startSuffix, retries));
    }

    public Callable<Collection<ProjectResponse>> getProjects(String spaceName) {
        return new Callable<Collection<ProjectResponse>>() {
            @Override
            public Collection<ProjectResponse> call() {
                return workbenchClient.getProjects(spaceName);
            }
        };
    }

    public Callable<Void> deleteProjects(String spaceName) {
        return deleteProjects(spaceName, allCreatedProjects);
    }

    public Callable<Void> deleteProjects(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(name->{
                    workbenchClient.deleteProject(spaceName, name);
                });
                return null;
            }
        };
    }

    public Callable<Collection<String>> createProjectsWithDelays(String spaceName, String projectName, int startSuffix, int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                List<String> createdProjects = new ArrayList<>(retries);
                for (int i = startSuffix; i < startSuffix + retries; i++) {
                    workbenchClient.createProject(spaceName,projectName + "-" + i, GROUP_ID, VERSION);
                    createdProjects.add(projectName + "-" + i);
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } // random value in (0,5 s - 3 s)
                }
                allCreatedProjects.addAll(createdProjects);
                return createdProjects;
            }
        };
    }

    public Callable<Void> deleteProjectsWithDelays(String spaceName) {
        return deleteProjectsWithDelays(spaceName, allCreatedProjects);
    }

    public Callable<Void> deleteProjectsWithDelays(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(name->{
                    workbenchClient.deleteProject(spaceName, name);
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } // random value in (0,5 s - 3 s)
                });
                return null;
            }
        };
    }

}
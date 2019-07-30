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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.kie.cloud.api.deployment.WorkbenchDeployment;

public class CompileRunner extends AbstractRunner {

    public CompileRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        super(workbenchDeployment, user, password);
        // TODO Auto-generated constructor stub
    }

    public Callable<Void> compileProjects(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(project -> {
                    workbenchClient.compileProject(spaceName, project);
                });
                return null;
            }
        };
    }

    public Callable<Void> installProjects(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(project -> {
                    workbenchClient.installProject(spaceName, project);
                });
                return null;
            }
        };
    }

    public Callable<Void> testProjects(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(project -> {
                    workbenchClient.testProject(spaceName, project);
                });
                return null;
            }
        };
    }

    public Callable<Void> deployProjects(String spaceName, Collection<String> projectNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                projectNames.forEach(project -> {
                    workbenchClient.deployProject(spaceName, project);
                });
                return null;
            }
        };
    }

}
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

import org.guvnor.rest.client.Space;
import org.kie.cloud.api.deployment.WorkbenchDeployment;

// Runner class witch can run command in series repeatedly as a new thread
public class SpaceRunner extends AbstractRunner {

    public SpaceRunner(WorkbenchDeployment workbenchDeployment, String user, String password) {
        super(workbenchDeployment, user, password);
        // TODO Auto-generated constructor stub
    }

    public Callable<Collection<String>> createSpaces(String spaceName, int startSuffix, int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                List<String> createdSpaces = new ArrayList<>(retries);
                for (int i = startSuffix; i < startSuffix + retries; i++) {
                    workbenchClient.createSpace(spaceName + "-" + i, wbUser);
                    createdSpaces.add(spaceName + "-" + i);
                }
                return createdSpaces;
            }
        };
    }

    public Callable<Collection<Space>> getAllSpaces() {
        return new Callable<Collection<Space>>() {
            @Override
            public Collection<Space> call() {
                return workbenchClient.getSpaces();
            }
        };
    }

    public Callable<Void> deleteSpaces(Collection<String> spaceNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                spaceNames.forEach(workbenchClient::deleteSpace);
                return null;
            }
        };
    }

    public Callable<Collection<String>> createSpacesWithDelays(String spaceName, int startSuffix, int retries) {
        return new Callable<Collection<String>>() {
            @Override
            public Collection<String> call() {
                List<String> createdSpaces = new ArrayList<>(retries);
                for (int i = startSuffix; i < startSuffix + retries; i++) {
                    workbenchClient.createSpace(spaceName + "-" + i, wbUser);
                    createdSpaces.add(spaceName + "-" + i);
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } // random value in (0,5 s - 3 s)
                }
                return createdSpaces;
            }
        };
    }

    public Callable<Void> deleteSpacesWithDelays(Collection<String> spaceNames) {
        return new Callable<Void>() {
            @Override
            public Void call() {
                spaceNames.forEach(space->{
                    workbenchClient.deleteSpace(space);
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
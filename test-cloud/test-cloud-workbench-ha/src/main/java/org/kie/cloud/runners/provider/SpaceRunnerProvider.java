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

package org.kie.cloud.runners.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.runners.SpaceRunner;
import org.kie.cloud.util.Users;

// Runner class witch can run command in series repeatedly as a new thread
public class SpaceRunnerProvider {

    public static List<SpaceRunner> getAllRunners(WorkbenchDeployment workbenchDeployment) {
        return Stream.of(Users.class.getEnumConstants())
                     .map(user -> {return new SpaceRunner(workbenchDeployment, user.getName(), user.getPassword());})
                     .collect(Collectors.toList());
        
    }

    public static List<SpaceRunner> getOneRunners(WorkbenchDeployment workbenchDeployment) {
        List<SpaceRunner> runners = new ArrayList<>();
        runners.add(new SpaceRunner(workbenchDeployment, Users.JOHN.getName(), Users.JOHN.getPassword()));
        return runners;
    }
    //Create Runners with different users.
    //List<SpaceRunner> runners = new ArrayList<>();
    //Stream.of(Users.class.getEnumConstants()).forEach(user -> {runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), user.getName(), user.getPassword()));});
    /*
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.JOHN.getName(), Users.JOHN.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.FRODO.getName(), Users.FRODO.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.SAM.getName(), Users.SAM.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.MERRY.getName(), Users.MERRY.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.PIPPIN.getName(), Users.PIPPIN.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.ARAGORN.getName(), Users.ARAGORN.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.LEGOLAS.getName(), Users.LEGOLAS.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.GIMLI.getName(), Users.GIMLI.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.GANDALF.getName(), Users.GANDALF.getPassword()));
    runners.add(new SpaceRunner(deploymentScenario.getWorkbenchDeployment(), Users.BOROMIR.getName(), Users.BOROMIR.getPassword()));
    */
    //... TODO
    
}
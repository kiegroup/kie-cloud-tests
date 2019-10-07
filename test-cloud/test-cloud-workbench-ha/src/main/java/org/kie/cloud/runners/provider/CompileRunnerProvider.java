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
import java.util.stream.Stream;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.runners.CompileRunner;
import org.kie.cloud.util.Users;


public class CompileRunnerProvider {

    public static List<CompileRunner> getAllRunners(WorkbenchDeployment workbenchDeployment) {
        List<CompileRunner> runners = new ArrayList<>();
        Stream.of(Users.class.getEnumConstants())
              .forEach(user -> {runners.add(new CompileRunner(workbenchDeployment, user.getName(), user.getPassword()));});
        return runners;
    }

    public static List<CompileRunner> getOneRunners(WorkbenchDeployment workbenchDeployment) {
        List<CompileRunner> runners = new ArrayList<>();
        runners.add(new CompileRunner(workbenchDeployment, Users.JOHN.getName(), Users.JOHN.getPassword()));
        return runners;
    }
        //Create Runners with different users.
        //List<CompileRunner> runners = new ArrayList<>();
        //runners.add(new CompileRunner(deploymentScenario.getWorkbenchDeployment(), Users.JOHN.getName(), Users.JOHN.getPassword()));
        //... TODO

}
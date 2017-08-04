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

package org.kie.cloud.integrationtests.util;

import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.git.GitProvider;
import org.kie.wb.test.rest.client.RestWorkbenchClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class WorkbenchUtils {

    private static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    private static final String REPOSITORY_NAME = "myRepo";

    // Path relative to target/classes folder
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    public static void deployProjectToWorkbench(GitProvider gitProvider, WorkbenchDeployment workbenchDeployment, String projectName) {
        gitProvider.createGitRepository(workbenchDeployment.getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER).getFile());

        WorkbenchClient workbenchClient = RestWorkbenchClient.createWorkbenchClient(workbenchDeployment.getUrl().toString(), workbenchDeployment.getUsername(), workbenchDeployment.getPassword());
        workbenchClient.createOrganizationalUnit(ORGANIZATION_UNIT_NAME, workbenchDeployment.getUsername());
        workbenchClient.cloneRepository(ORGANIZATION_UNIT_NAME, REPOSITORY_NAME, gitProvider.getRepositoryUrl(workbenchDeployment.getNamespace()));
        workbenchClient.deployProject(REPOSITORY_NAME, projectName);
    }
}

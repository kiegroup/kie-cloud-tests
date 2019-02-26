/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.integrationtests.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.xtf.openshift.OpenShiftBinaryClient;

@RunWith(Parameterized.class)
public class WorkbenchGitHooksPersistenceIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchGitHooksPersistenceIntegrationTest.class);

    @Parameterized.Parameter(value = 0)
    public String testScenarioName;

    @Parameterized.Parameter(value = 1)
    public KieDeploymentScenario<?> workbenchKieServerScenario;

    private WorkbenchClient workbenchClient;

    private WorkbenchDeployment workbenchDeployment;

    private static final String GIT_HOOKS_REMOTE_DIR = "/opt/eap/standalone/data/kie/git/hooks";

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            WorkbenchKieServerPersistentScenario workbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .withGitHooksDir(GIT_HOOKS_REMOTE_DIR)
                .build();
            scenarios.add(new Object[] { "Workbench + KIE Server - Persistent", workbenchKieServerPersistentScenario });
        } catch (UnsupportedOperationException ex) {
            logger.info("Workbench + KIE Server - Persistent is skipped.", ex);
        }

        try {
            ClusteredWorkbenchKieServerDatabasePersistentScenario clusteredWorkbenchKieServerDatabasePersistentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                .withGitHooksDir(GIT_HOOKS_REMOTE_DIR)
                .build();
            scenarios.add(new Object[]{"Clustered Workbench + KIE Server + Database - Persistent", clusteredWorkbenchKieServerDatabasePersistentScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("Clustered Workbench + KIE Server + Database - Persistent scenario is skipped.", ex);
        }

        return scenarios;
    }

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    @Before
    public void setUp() {
        workbenchDeployment = deploymentScenario.getWorkbenchDeployments().get(0);
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(workbenchDeployment);
    }

    @Test
    public void testGitHooksPersistence() {
        List<String> workbenchInstanceNames = workbenchDeployment.getInstances().stream().map(Instance::getName).collect(Collectors.toList());

        logger.info("Copy git hooks to the pod");
        Path localResourcesDir = Paths.get(ClassLoader.class.getResource("/git-hooks").getPath());
        rsync(workbenchInstanceNames.get(0), localResourcesDir, GIT_HOOKS_REMOTE_DIR, true);

        String projectName = "testGitHooksPersistenceProject";
        workbenchClient.createSpace(SPACE_NAME, workbenchDeployment.getUsername());
        workbenchClient.createProject(SPACE_NAME, projectName, PROJECT_GROUP_ID, "1.0");
        assertSpaceAndProjectExists(SPACE_NAME, projectName);

        logger.debug("Copy output from git hook back to locat test resources dir");
        rsync(workbenchInstanceNames.get(0), localResourcesDir, GIT_HOOKS_REMOTE_DIR, false);
        long linesOfOutput = checkOutputFile(0);

        scaleToZeroAndToOne(workbenchDeployment);

        workbenchInstanceNames = workbenchDeployment.getInstances().stream().map(Instance::getName).collect(Collectors.toList());

        assertSpaceAndProjectExists(SPACE_NAME, projectName);
        String newProjectName = "newTestGitHooksPersistenceProject";
        workbenchClient.createProject(SPACE_NAME, newProjectName, PROJECT_GROUP_ID, "1.0");
        assertSpaceAndProjectExists(SPACE_NAME, newProjectName);

        logger.debug("Copy output from git hook back to locat test resources dir");
        rsync(workbenchInstanceNames.get(0), localResourcesDir, GIT_HOOKS_REMOTE_DIR, false);
        checkOutputFile(linesOfOutput);

    }

    private void assertSpaceAndProjectExists(String spaceName, String projectName) {
        Collection<Space> spaces = workbenchClient.getSpaces();
        assertThat(spaces.stream().anyMatch(n -> n.getName().equals(spaceName))).as("Space " + spaceName + " not found.").isTrue();

        Collection<ProjectResponse> projects = workbenchClient.getProjects(spaceName);
        assertThat(projects.stream().anyMatch(n -> n.getName().equals(projectName))).as("Project " + projectName + " not found.").isTrue();
    }

    private void scaleToZeroAndToOne(Deployment deployment) {
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(1);
        deployment.waitForScale();
    }

    private long checkOutputFile(long previousLineCount) {
        long newLineCount = 0;
        try {
            logger.debug("Print out post-commit output file");
            Files.lines(Paths.get(ClassLoader.class.getResource("/git-hooks/out.txt").getPath())).forEach((line) -> {
                logger.debug(line);
            });
            newLineCount = Files.lines(Paths.get(ClassLoader.class.getResource("/git-hooks/out.txt").getPath())).count();
            Files.delete(Paths.get(ClassLoader.class.getResource("/git-hooks/out.txt").getPath()));
        } catch (IOException ex) {
            throw new RuntimeException("IOException wile reading output file.", ex);
        }
        assertThat(newLineCount).isGreaterThan(previousLineCount);
        return newLineCount;
    }

    private void rsync(final String podName, final Path localDir, final String remoteDir, boolean toPod) {
        OpenShiftBinaryClient oc = OpenShiftBinaryClient.getInstance();
        oc.project(deploymentScenario.getNamespace());

        List<String> args = new ArrayList<>();
        args.add("rsync");

        args.add(!toPod ? (podName + ":" + remoteDir + "/") : localDir.toFile()
                .getAbsoluteFile().getPath() + "/");
        args.add(toPod ? (podName + ":" + remoteDir + "/") : localDir.toFile()
                .getAbsoluteFile().getPath() + "/");
        oc.executeCommand("rsync has failed", args.toArray(new String[args.size()]));
    }

}

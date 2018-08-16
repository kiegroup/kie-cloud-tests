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
package org.kie.cloud.integrationtests.persistence;

import cz.xtf.openshift.OpenShiftBinaryClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.Space;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.scenario.ClusteredWorkbenchKieServerDatabasePersistentScenario;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.Kjar;
import org.kie.cloud.integrationtests.util.Constants;
import org.kie.cloud.integrationtests.util.WorkbenchUtils;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
@Ignore("Ignored as the tests are affected by RHPAM-1354. Unignore when the JIRA will be fixed.")
public class WorkbenchPersistenceIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<DeploymentScenario> {

    private static final Logger logger = LoggerFactory.getLogger(WorkbenchPersistenceIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public DeploymentScenario workbenchKieServerScenario;

    private String repositoryName;

    private WorkbenchClient workbenchClient;
    private KieServerControllerClient kieControllerClient;
    private KieServicesClient kieServerClient;

    private WorkbenchDeployment workbenchDeployment;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        WorkbenchKieServerPersistentScenario workbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .build();

        ClusteredWorkbenchKieServerDatabasePersistentScenario clusteredWorkbenchKieServerDatabasePersistentScenario = deploymentScenarioFactory.getClusteredWorkbenchKieServerDatabasePersistentScenarioBuilder()
                .build();

        return Arrays.asList(new Object[][]{
            {"Workbench + KIE Server - Persistent", workbenchKieServerPersistentScenario},
            {"Clustered Workbench + KIE Server + Database - Persistent", clusteredWorkbenchKieServerDatabasePersistentScenario},
        });
    }

    @Override
    protected DeploymentScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    @Before
    public void setUp() {
        workbenchDeployment = deploymentScenario.getWorkbenchDeployments().get(0);
        workbenchClient = WorkbenchClientProvider.getWorkbenchClient(workbenchDeployment);
        kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(workbenchDeployment);
        kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployments().get(0));
    }

    @After
    public void tearDown() {
        if (repositoryName != null) {
            gitProvider.deleteGitRepository(repositoryName);
            repositoryName = null;
        }
    }

    @Test
    public void testWorkbenchControllerPersistence() {
        repositoryName = gitProvider.createGitRepositoryWithPrefix(workbenchDeployment.getNamespace(), ClassLoader.class.getResource(PROJECT_SOURCE_FOLDER + "/" + DEFINITION_PROJECT_NAME).getFile());

        WorkbenchUtils.deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), workbenchDeployment, DEFINITION_PROJECT_NAME);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        String kieServerLocation = serverInfo.getLocation();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);

        scaleToZeroAndToOne(workbenchDeployment);

        verifyOneServerTemplateWithContainer(kieServerLocation, CONTAINER_ID);
    }

    @Test
    public void testWorkbenchProjectPersistence() {
        workbenchClient.createSpace(SPACE_NAME, workbenchDeployment.getUsername());
        workbenchClient.createProject(SPACE_NAME, DEFINITION_PROJECT_NAME, PROJECT_GROUP_ID, DEFINITION_PROJECT_VERSION);

        assertSpaceAndProjectExists(SPACE_NAME, DEFINITION_PROJECT_NAME);

        scaleToZeroAndToOne(workbenchDeployment);

        assertSpaceAndProjectExists(SPACE_NAME, DEFINITION_PROJECT_NAME);
        workbenchClient.deployProject(SPACE_NAME, DEFINITION_PROJECT_NAME);

        scaleToZeroAndToOne(workbenchDeployment);

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, Kjar.DEFINITION, KieContainerStatus.STARTED);

        KieServerClientProvider.waitForContainerStart(deploymentScenario.getKieServerDeployments().get(0), CONTAINER_ID);

        ServiceResponse<KieContainerResourceList> containersResponse = kieServerClient.listContainers();
        assertThat(containersResponse.getType()).isEqualTo(ResponseType.SUCCESS);
        assertThat(containersResponse.getResult().getContainers()).hasSize(1);
        assertThat(containersResponse.getResult().getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
    }

    @Test
    public void testGitHooksPersistence() {
        // get git hook dir location
        String gitHooksRemoteDir = "/opt/eap/standalone/data/bpmsuite/git/hooks"; //TODO for 7.1 bpmsuite -> kie

        updateWorkbenchDeployment(gitHooksRemoteDir);

        logger.debug("Filter instances for second deployment");
        List<String> workbenchInstanceNames = workbenchDeployment.getInstances().stream().map(Instance::getName).filter(name -> name.contains("-2-")).collect(Collectors.toList());
        if (workbenchInstanceNames.isEmpty()) {
            throw new RuntimeException("Workbench was not correctly redeployed after update.");
        }

        logger.info("Copy git hooks to the pod");
        Path localResourcesDir = Paths.get(ClassLoader.class.getResource("/git-hooks").getPath());
        mkdir(workbenchInstanceNames.get(0), gitHooksRemoteDir);
        rsync(workbenchInstanceNames.get(0), localResourcesDir, gitHooksRemoteDir, true);

        String projectName = "testGitHooksPersistenceProject";
        workbenchClient.createSpace(SPACE_NAME, workbenchDeployment.getUsername());
        workbenchClient.createProject(SPACE_NAME, projectName, PROJECT_GROUP_ID, "1.0");
        assertSpaceAndProjectExists(SPACE_NAME, projectName);

        logger.debug("Copy output from git hook back to locat test resources dir");
        rsync(workbenchInstanceNames.get(0), localResourcesDir, gitHooksRemoteDir, false);
        long linesOfOutput = checkOutputFile(0);

        scaleToZeroAndToOne(workbenchDeployment);

        assertSpaceAndProjectExists(SPACE_NAME, projectName);
        String newProjectName = "newTestGitHooksPersistenceProject";
        workbenchClient.createProject(SPACE_NAME, newProjectName, PROJECT_GROUP_ID, "1.0");
        assertSpaceAndProjectExists(SPACE_NAME, newProjectName);

        logger.debug("Copy output from git hook back to locat test resources dir");
        rsync(workbenchInstanceNames.get(0), localResourcesDir, gitHooksRemoteDir, false);
        checkOutputFile(linesOfOutput);

    }

    private void verifyOneServerTemplateWithContainer(String kieServerLocation, String containerId) {
        ServerTemplateList serverTemplates = kieControllerClient.listServerTemplates();
        assertThat(serverTemplates.getServerTemplates()).as("Number of server templates differ.").hasSize(1);

        ServerTemplate serverTemplate = serverTemplates.getServerTemplates()[0];
        assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
        assertThat(serverTemplate.getServerInstanceKeys().iterator().next().getUrl()).isEqualTo(kieServerLocation);
        assertThat(serverTemplate.getContainersSpec()).hasSize(1);
        assertThat(serverTemplate.getContainersSpec().iterator().next().getId()).isEqualTo(containerId);
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
            // TODO: check output of hooks scripts
            // https://issues.jboss.org/browse/AF-1401 wait for fix included in 7.1
            Files.lines(Paths.get(ClassLoader.class.getResource("/git-hooks/out.txt").getPath())).forEach((line) -> {
                System.out.println(line);

            });
            newLineCount = Files.lines(Paths.get(ClassLoader.class.getResource("/git-hooks/out.txt").getPath())).count();
        } catch (IOException ex) {
            throw new RuntimeException("IOException wile reading output file.", ex);
        }
        assertThat(newLineCount).isGreaterThan(previousLineCount);
        return newLineCount;
    }

    private void updateWorkbenchDeployment(String gitHooksRemoteDir) {
        logger.info("Updating workbench deployment");
        Map<String, String> updatedEnvVariables = new HashMap<>();
        updatedEnvVariables.put(Constants.BusinessCentralImage.GIT_HOOKS_DIR, gitHooksRemoteDir);
        workbenchDeployment.updateDeploymentConfig(updatedEnvVariables);

        logger.info("Wait for Workbench redeployment");
        try {
            logger.debug("Wait for a few seconds to let OpenShift redeploy Workbench");
            Thread.sleep(3000L);
        } catch (InterruptedException ex) {
            logger.warn("InterruptedException while waitng for OpenShift redeploy", ex);
        }
        workbenchDeployment.waitForScale();
    }

    // TODO: try to move commands to instances
    private void mkdir(final String podName, final String remoteDir) {
        OpenShiftBinaryClient oc = OpenShiftBinaryClient.getInstance();
        oc.project(deploymentScenario.getNamespace());

        // TODO: report git hooks dir is nor created when paramaeter GIT HOOKS is set
        // https://issues.jboss.org/browse/RHPAM-1479
        List<String> mkdirArg = new ArrayList<>();
        mkdirArg.add("rsh");
        mkdirArg.add(podName);
        mkdirArg.add("mkdir");
        mkdirArg.add("-p");
        mkdirArg.add(remoteDir);
        oc.executeCommand("rsh make remote dir failed", mkdirArg.toArray(new String[mkdirArg.size()]));
    }

    // oc rsync ./local/dir <pod-name>:/remote/dir
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

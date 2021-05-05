/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.springboot.integrationtests;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessIntegrationTest.class);

    private static final String APPLICATION_NAME = "custom-springboot-app";
    private static final String APPLICATION_IMAGE_TAG = String.format("%s/openshift/%s:latest", OpenShiftController.getImageRegistryRouteHostname(), APPLICATION_NAME);

    private static final String CONTAINER_ID = "definition-project";
    private static final String PROCESS_ID = "definition-project.usertask";
    private static final String USER_YODA = "yoda";

    private Project project;
    private KieServerDeployment kieServerDeployment;

    @BeforeClass
    public static void prepareImage() {
        // Build kjar and deploy it to dedicated repository (settings.xml location provided in system properties)
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");

        try (ProcessExecutor executor = new ProcessExecutor()) {
            // Build Docker image
            String dockerfileLocation = System.getProperty("dockerfile.location");
            String containerEngine = System.getProperty("container.engine");
            boolean executionSuccessful = executor.executeProcessCommand(String.format("%s build -t %s .", containerEngine, APPLICATION_IMAGE_TAG), Paths.get(dockerfileLocation));
            if (!executionSuccessful) {
                throw new RuntimeException("Error while building image from from location " + dockerfileLocation);
            }

            // Push image to registry
            if ("docker".equals(containerEngine)) {
                loginToInternalDockerRegistry(executor);
                executionSuccessful = executor.executeProcessCommand(String.format("docker push %s", APPLICATION_IMAGE_TAG));
            } else {
                executionSuccessful = executor.executeProcessCommand(String.format("%s push %s --tls-verify=false", containerEngine, APPLICATION_IMAGE_TAG));
            }
            if (!executionSuccessful) {
                throw new RuntimeException("Error while pushing image " + APPLICATION_IMAGE_TAG);
            }
        }
    }

    private static void loginToInternalDockerRegistry(ProcessExecutor executor) {
        logger.info("Creating temporary project, so we can get token through OpenShiftBinary client.");
        Project tmpProject = OpenShiftController.createProject("tmp-project-"+UUID.randomUUID().toString().substring(0, 4));
        try {
            String username = tmpProject.runOcCommandAsAdmin("whoami").trim();
            String password = tmpProject.runOcCommandAsAdmin("whoami", "--show-token").trim();

            executor.executeProcessCommand(String.format("docker login -u %s -p %s %s", username, password , OpenShiftController.getImageRegistryRouteHostname()));
        } finally {
            tmpProject.delete();
        }
    }

    @Before
    public void prepareProject() {
        String projectSuffix = UUID.randomUUID().toString().substring(0, 4);
        String projectName = OpenShiftConstants.getNamespacePrefix().map(p -> p + "-" + projectSuffix).orElse(projectSuffix);

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        project = OpenShiftController.createProject(projectName);

        logger.info("Installing SpringBoot application " + APPLICATION_NAME);
        project.runOcCommand("new-app", APPLICATION_NAME);

        logger.info("Exposing SpringBoot application " + APPLICATION_NAME);
        project.runOcCommand("expose", "svc/" + APPLICATION_NAME);

        kieServerDeployment = getKieServerDeployment();
        kieServerDeployment.waitForScale();
    }

    @After
    public void cleanupProject() throws Exception {
        project.delete();
        project.close();
    }

    @Test
    public void completeProcess() {
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(kieServerDeployment, "/rest/server");
        ProcessServicesClient processClient = kieServerClient.getServicesClient(ProcessServicesClient.class);
        UserTaskServicesClient taskClient = kieServerClient.getServicesClient(UserTaskServicesClient.class);

        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID);
        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        taskClient.completeAutoProgress(CONTAINER_ID, tasks.get(0).getId(), USER_YODA, null);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
        Assertions.assertThat(processInstance).isNotNull();
        Assertions.assertThat(processInstance.getState().intValue()).isEqualTo(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED);
    }

    private KieServerDeployment getKieServerDeployment() {
        KieServerDeploymentImpl kieServerDeployment = new KieServerDeploymentImpl(project) {
            @Override
            public String getServiceName() {
                return APPLICATION_NAME;
            }
        };
        kieServerDeployment.setUsername(DeploymentConstants.getAppUser());
        kieServerDeployment.setPassword(DeploymentConstants.getAppPassword());
        return kieServerDeployment;
    }
}

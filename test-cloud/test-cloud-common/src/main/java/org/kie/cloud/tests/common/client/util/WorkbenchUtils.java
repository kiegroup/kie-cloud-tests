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

package org.kie.cloud.tests.common.client.util;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.guvnor.rest.client.CloneProjectRequest;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.git.GitProvider;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.tests.common.curl.CurlCommand;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.wb.test.rest.client.WorkbenchClient;

public class WorkbenchUtils {

    private static final String SPACE_NAME = "mySpace";

    private static final Duration WAIT_STEP = Duration.ofSeconds(1);
    private static final Duration MAX_WAIT_DURATION = Duration.ofSeconds(15);

    public static void deployProjectToWorkbench(String repositoryName, KieDeploymentScenario<?> deploymentScenario, String projectName) {
        GitProvider gitProvider = deploymentScenario.getGitProvider();
        WorkbenchDeployment workbenchDeployment = deploymentScenario.getWorkbenchDeployments().get(0);

        deployProjectToWorkbench(gitProvider.getRepositoryUrl(repositoryName), workbenchDeployment, projectName);
    }

    public static void deployProjectToWorkbench(String repositoryUrl, WorkbenchDeployment workbenchDeployment, String projectName) {
        CloneProjectRequest cloneProjectRequest = createCloneProjectRequest(repositoryUrl, projectName);

        WorkbenchClient workbenchClient = WorkbenchClientProvider.getWorkbenchClient(workbenchDeployment);
        workbenchClient.createSpace(SPACE_NAME, workbenchDeployment.getUsername());
        waitUntilSpaceIsSynchronised(workbenchDeployment, SPACE_NAME);
        workbenchClient.cloneRepository(SPACE_NAME, cloneProjectRequest);
        workbenchClient.deployProject(SPACE_NAME, projectName);
    }

    /**
     * Wait until the space is present in all the workbench pods.
     * @param workbenchDeployment workbench deployment
     * @param spaceName space
     */
    public static void waitUntilSpaceIsSynchronised(WorkbenchDeployment workbenchDeployment, String spaceName) {
        workbenchDeployment.getInstances().forEach(i -> {
            TimeUtils.wait(MAX_WAIT_DURATION, WAIT_STEP, () -> {
                String spaces = CurlCommand.onInstance(i)
                                           .withUsername(workbenchDeployment.getUsername())
                                           .withPassword(workbenchDeployment.getPassword())
                                           .get("rest/spaces");
                return StringUtils.contains(spaces, spaceName);
            });
        });
    }

    private static CloneProjectRequest createCloneProjectRequest(String repositoryUrl, String projectName) {
        CloneProjectRequest cloneProjectRequest = new CloneProjectRequest();
        cloneProjectRequest.setGitURL(repositoryUrl);
        cloneProjectRequest.setName(projectName);
        return cloneProjectRequest;
    }

    public static void waitForContainerRegistration(KieServerControllerClient kieControllerClient, String serverTemplate, String containerId) {
        TimeUtils.wait(MAX_WAIT_DURATION, WAIT_STEP, () -> {
            Collection<ContainerSpec> containersSpec = kieControllerClient.getServerTemplate(serverTemplate).getContainersSpec();
            return containersSpec.stream().anyMatch(n -> n.getId().equals(containerId));
        });
    }

    public static void saveContainerSpec(KieServerControllerClient kieControllerClient, String serverTemplateId, String serverTemplateName, String containerId, String containerAlias, Kjar kjar, KieContainerStatus status) {
        saveContainerSpec(kieControllerClient, serverTemplateId, serverTemplateName, containerId, containerAlias, kjar, status, Collections.emptyMap());
    }

    public static void saveContainerSpec(KieServerControllerClient kieControllerClient, String serverTemplateId, String serverTemplateName, String containerId, String containerAlias, Kjar kjar, KieContainerStatus status, Map<Capability, ContainerConfig> configs) {
        ServerTemplateKey serverTemplateKey = new ServerTemplateKey(serverTemplateId, serverTemplateName);
        ReleaseId releasedId = new ReleaseId(kjar.getGroupId(), kjar.getArtifactName(), kjar.getVersion());
        ContainerSpec containerSpec = new ContainerSpec(containerId, containerAlias, serverTemplateKey, releasedId, status, configs);
        kieControllerClient.saveContainerSpec(serverTemplateId, containerSpec);
    }
}

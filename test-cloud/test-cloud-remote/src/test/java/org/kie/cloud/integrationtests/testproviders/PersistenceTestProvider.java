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
package org.kie.cloud.integrationtests.testproviders;

import java.util.Objects;

import org.assertj.core.api.SoftAssertions;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.scenario.DeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.KieServerControllerClientProvider;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.cloud.tests.common.client.util.WorkbenchUtils;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.client.KieServicesClient;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.KieServerControllerClient;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceTestProvider {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceTestProvider.class);

    private PersistenceTestProvider() {}

    /**
     * Create provider instance
     * 
     * @return provider instance
     */
    public static PersistenceTestProvider create() {
        return create(null);
    }

    /**
     * Create provider instance and init it with given environment
     * 
     * @param environment if not null, initialize this provider with the environment
     * 
     * @return provider instance
     */
    public static PersistenceTestProvider create(DeploymentScenario<?> deploymentScenario) {
        PersistenceTestProvider provider = new PersistenceTestProvider();
        if (Objects.nonNull(deploymentScenario)) {
            provider.init(deploymentScenario);
        }
        return provider;
    }

    private void init(DeploymentScenario<?> deploymentScenario) {}

    public void testControllerPersistence(WorkbenchKieServerScenario deploymentScenario) {
        String containerId = "testControllerPersistence";

        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        String kieServerLocation = serverInfo.getLocation();
        try {
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), containerId, containerId, Kjar.DEFINITION, KieContainerStatus.STARTED);

            verifyOneServerTemplateWithContainer(kieControllerClient, kieServerLocation, containerId);

            scaleToZeroAndBackToOne(deploymentScenario.getWorkbenchDeployment());

            verifyOneServerTemplateWithContainer(kieControllerClient, kieServerLocation, containerId);
        } finally {
            kieControllerClient.deleteContainerSpec(serverInfo.getServerId(), containerId);
        }
    }


    // Verifies https://issues.redhat.com/browse/RHPAM-2762
    public void testAdminUserPasswordChange(WorkbenchKieServerScenario deploymentScenario) {
        final String NEW_PASSWORD = "newpassword1234!";
        final String oldPassword = deploymentScenario.getWorkbenchDeployment().getPassword();

        final String username = deploymentScenario.getWorkbenchDeployment().getUsername();
        String containerId = "testAdminUserPasswordChange";
        final String workbenchUrl = deploymentScenario.getWorkbenchDeployment().getUrl().toString();
        final String kieServerUrl = deploymentScenario.getKieServerDeployment().getUrl().toString();

        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        try {
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), containerId, containerId, Kjar.DEFINITION, KieContainerStatus.STARTED);
            // Check if old user can connect
            SoftAssertions.assertSoftly(softly -> {
                softly.assertAlso(isUserAuthorizedInWorkbench(workbenchUrl, username, oldPassword));
                softly.assertAlso(isUserAuthorizedInKieServer(kieServerUrl, username, oldPassword));
            });

            logger.info("Change username to {} and password to {} in credential secret", username, NEW_PASSWORD);
            deploymentScenario.changeUsernameAndPassword(username, NEW_PASSWORD);
            //workbenchDeployment.changeUsernameAndPassword(NEW_USERNAME, NEW_PASSWORD);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertAlso(isUserUnauthorizedInWorkbench(workbenchUrl, username, oldPassword));

                softly.assertAlso(isUserAuthorizedInWorkbench(workbenchUrl, username, NEW_PASSWORD));

                softly.assertAlso(isUserUnauthorizedInKieServer(kieServerUrl, username, oldPassword));

                softly.assertAlso(isUserAuthorizedInKieServer(kieServerUrl, username, NEW_PASSWORD));
            });
        } finally {
            deploymentScenario.changeUsernameAndPassword(username, oldPassword);
            kieControllerClient.deleteContainerSpec(serverInfo.getServerId(), containerId);
        }
    }

    // Verifies https://issues.redhat.com/browse/RHPAM-2777
    public void testAdminUserNameAndPasswordChange(WorkbenchKieServerScenario deploymentScenario) {
        final String NEW_USERNAME = "newadminusername";
        final String NEW_PASSWORD = "newpassword4321!";

        final String oldUsername = deploymentScenario.getWorkbenchDeployment().getUsername();
        final String oldPassword = deploymentScenario.getWorkbenchDeployment().getPassword();
        String containerId = "testAdminUserNameAndPasswordChange";
        final String workbenchUrl = deploymentScenario.getWorkbenchDeployment().getUrl().toString();
        final String kieServerUrl = deploymentScenario.getKieServerDeployment().getUrl().toString();

        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(deploymentScenario.getWorkbenchDeployment());
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());

        KieServerInfo serverInfo = kieServerClient.getServerInfo().getResult();
        try {
            WorkbenchUtils.saveContainerSpec(kieControllerClient, serverInfo.getServerId(), serverInfo.getName(), containerId, containerId, Kjar.DEFINITION, KieContainerStatus.STARTED);
            // Check if old user can connect
            SoftAssertions.assertSoftly(softly -> {
                softly.assertAlso(isUserAuthorizedInWorkbench(workbenchUrl, oldUsername, oldPassword));
                softly.assertAlso(isUserAuthorizedInKieServer(kieServerUrl, oldUsername, oldPassword));
            });

            logger.info("Change username to {} and password to {} in credential secret", NEW_USERNAME, NEW_PASSWORD);
            deploymentScenario.changeUsernameAndPassword(NEW_USERNAME, NEW_PASSWORD);
            //workbenchDeployment.changeUsernameAndPassword(NEW_USERNAME, NEW_PASSWORD);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertAlso(isUserUnauthorizedInWorkbench(workbenchUrl, oldUsername, oldPassword));
                softly.assertAlso(isUserUnauthorizedInWorkbench(workbenchUrl, oldUsername, NEW_PASSWORD));
                softly.assertAlso(isUserUnauthorizedInWorkbench(workbenchUrl, NEW_USERNAME, oldPassword));

                softly.assertAlso(isUserAuthorizedInWorkbench(workbenchUrl, NEW_USERNAME, NEW_PASSWORD));

                softly.assertAlso(isUserUnauthorizedInKieServer(kieServerUrl, oldUsername, oldPassword));
                softly.assertAlso(isUserUnauthorizedInKieServer(kieServerUrl, oldUsername, NEW_PASSWORD));
                softly.assertAlso(isUserUnauthorizedInKieServer(kieServerUrl, NEW_USERNAME, oldPassword));

                softly.assertAlso(isUserAuthorizedInKieServer(kieServerUrl, NEW_USERNAME, NEW_PASSWORD));
            });
        } finally {
            deploymentScenario.changeUsernameAndPassword(oldUsername, oldPassword);
            kieControllerClient.deleteContainerSpec(serverInfo.getServerId(), containerId);
        }
    }

    private SoftAssertions isUserAuthorizedInWorkbench(String url, String username, String password) {
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(url, username, password);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(kieControllerClient.listServerTemplates().getServerTemplates())
              .as("List Server Templates after login with username %s and password %s", username, password)
              .hasSize(1);
        return softly;
    }

    private SoftAssertions isUserUnauthorizedInWorkbench(String url, String username, String password) {
        KieServerControllerClient kieControllerClient = KieServerControllerClientProvider.getKieServerControllerClient(url, username, password);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThatThrownBy(() -> {
            kieControllerClient.listServerTemplates().getServerTemplates();
        }).as("Exception after login with username %s and password %s", username, password)
              .isInstanceOf(KieServerControllerHTTPClientException.class)
              .hasMessageContainingAll("401");
        return softly;
    }

    private SoftAssertions isUserAuthorizedInKieServer(String url, String username, String password) {
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(url, username, password);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(kieServerClient.getServerInfo().getResult())
              .as("GET server info of Kie Server login as username %s with password %s", username, password)
              .isNotNull();
        return softly;
    }

    private SoftAssertions isUserUnauthorizedInKieServer(String url, String username, String password) {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThatThrownBy(() -> {
            KieServerClientProvider.getKieServerClient(url, username, password);
        }).as("Exception create client for Kie Server login as username %s with password %s", username, password)
              .isInstanceOf(RuntimeException.class)
              .hasCauseInstanceOf(KieServicesHttpException.class)
              .hasMessageContainingAll("401", "Unauthorized");
        return softly;
    }

    private static void scaleToZeroAndBackToOne(Deployment deployment) {
        deployment.scale(0);
        deployment.waitForScale();
        deployment.scale(1);
        deployment.waitForScale();
    }

    private static void verifyOneServerTemplateWithContainer(KieServerControllerClient kieControllerClient, String kieServerLocation, String containerId) {
        ServerTemplateList serverTemplates = kieControllerClient.listServerTemplates();
        assertThat(serverTemplates.getServerTemplates()).as("Number of server templates differ.").hasSize(1);

        ServerTemplate serverTemplate = serverTemplates.getServerTemplates()[0];
        assertThat(serverTemplate.getServerInstanceKeys()).hasSize(1);
        assertThat(serverTemplate.getServerInstanceKeys().iterator().next().getUrl()).isEqualTo(kieServerLocation);
        assertThat(serverTemplate.getContainersSpec()).hasSize(1);
        assertThat(serverTemplate.getContainersSpec().iterator().next().getId()).isEqualTo(containerId);
    }
}

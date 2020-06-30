/*
 * Copyright 2020 JBoss by Red Hat.
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

package org.kie.cloud.integrationtests.s2i.jms;

import java.util.Arrays;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.scenario.WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario;
import org.kie.cloud.api.settings.GitSettings;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.git.GitUtils;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.MonitoringK8sFs;
import org.kie.cloud.integrationtests.s2i.KieServerS2iJbpmIntegrationTest;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.assertj.core.api.Assertions.assertThat;

@Category({JBPMOnly.class, MonitoringK8sFs.class})
public class KieServerS2iAmqExternalSignalIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String REPOSITORY_NAME = generateNameWithPrefix("KieServerS2iAmqExternalSignalRepository");

    private static WorkbenchRuntimeSmartRouterImmutableKieServerAmqWithDatabaseScenario deploymentScenario;

    private KieServicesClient kieServicesClient;
    private QueryServicesClient queryServicesClient;

    private static final String KIE_CONTAINER_DEPLOYMENT = CONTAINER_ID + "=" + Kjar.EXTERNAL_SIGNAL.toString();

    private static final String REPO_BRANCH = "master";
    private static final String PROJECT_SOURCE_FOLDER = "/kjars-sources";

    private static final String SIGNAL_QUEUE_JNDI = "queue/KIE.SERVER.SIGNAL";
    private static final String SIGNAL_NAME = "external";

    @BeforeClass
    public static void initializeDeployment() {
        try {
            deploymentScenario = deploymentScenarioFactory.getWorkbenchRuntimeSmartRouterImmutableKieServerAmqWithPostgreSqlScenarioBuilder()
                    .withContainerDeployment(KIE_CONTAINER_DEPLOYMENT)
                    .withGitSettings(GitSettings.fromProperties()
                                     .withRepository(REPOSITORY_NAME,
                                                     KieServerS2iJbpmIntegrationTest.class.getResource(PROJECT_SOURCE_FOLDER).getFile()))
                    .withSourceLocation(REPO_BRANCH, Kjar.EXTERNAL_SIGNAL.getArtifactName())
                    .enableExternalJmsSignalQueue(SIGNAL_QUEUE_JNDI)
                    .build();
        } catch (UnsupportedOperationException ex) {
            Assume.assumeFalse(ex.getMessage().startsWith("Not supported"));
        }

        deploymentScenario.setLogFolderName(KieServerS2iAmqExternalSignalIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        GitUtils.deleteGitRepository(REPOSITORY_NAME, deploymentScenario);
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Before
    public void setUp() {
        kieServicesClient = KieServerClientProvider.getKieServerJmsClient(deploymentScenario.getAmqDeployment().getTcpSslUrl());
        queryServicesClient = KieServerClientProvider.getQueryJmsClient(kieServicesClient);
    }

    @Test
    public void testContainerAfterExecServerS2IStart() throws Exception {
        checkNoProcessInstancesInKieServer();
        sendExternalSignal();
        checkProcessInstanceCompletedInKieServer();
    }

    private void checkNoProcessInstancesInKieServer() {
        List<ProcessInstance> processInstances = queryServicesClient.findProcessInstances(0, 10);
        assertThat(processInstances).isEmpty();
    }

    private void sendExternalSignal() throws Exception {
        ActiveMQSslConnectionFactory connectionFactory = KieServerClientProvider.getJmsConnectionFactory(deploymentScenario.getAmqDeployment().getTcpSslUrl());
        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Message message = session.createBytesMessage();
            message.setObjectProperty("KIE_Signal", SIGNAL_NAME);
            message.setObjectProperty("KIE_SignalDeploymentId", CONTAINER_ID);

            Queue sendQueue = session.createQueue(SIGNAL_QUEUE_JNDI);
            MessageProducer producer = session.createProducer(sendQueue);
            producer.send(message);
        }
    }

    private void checkProcessInstanceCompletedInKieServer() throws Exception {
        KieServerSynchronization.waitForProcessInstanceStart(queryServicesClient, CONTAINER_ID, 1, Arrays.asList(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED));
    }
}

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

package org.kie.cloud.common.provider;

import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.SmartRouterDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.SolverServicesClient;

public class KieServerClientProvider {

    private static final long KIE_SERVER_TIMEOUT = 300_000L;

    public static KieServicesClient getKieServerClient(KieServerDeployment kieServerDeployment) {
        return getKieServerClient(kieServerDeployment, KIE_SERVER_TIMEOUT);
    }

    public static KieServicesClient getKieServerClient(KieServerDeployment kieServerDeployment, long clientTimeout) {
        return getKieServerClient(kieServerDeployment, new HashSet<>(), clientTimeout);
    }

    public static KieServicesClient getKieServerClient(KieServerDeployment kieServerDeployment, Set<Class<?>> extraClasses) {
        return getKieServerClient(kieServerDeployment, extraClasses, KIE_SERVER_TIMEOUT);
    }

    public static KieServicesClient getKieServerClient(KieServerDeployment kieServerDeployment, Set<Class<?>> extraClasses, long clientTimeout) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(
                kieServerDeployment.getUrl().toString() + "/services/rest/server", kieServerDeployment.getUsername(),
                kieServerDeployment.getPassword(), clientTimeout);
        configuration.addExtraClasses(extraClasses);
        KieServicesClient kieServerClient = KieServicesFactory.newKieServicesClient(configuration);
        return kieServerClient;
    }

    public static KieServicesClient getKieServerJmsClient(URL amqHost) {
        return getKieServerJmsClient(amqHost, KIE_SERVER_TIMEOUT);
    }

    public static KieServicesClient getKieServerJmsClient(URL amqHost, long clientTimeout) {
        try {
            return getKieServerJmsClient(amqHost, new HashSet<>(), clientTimeout);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Kie Server JMS Client.", e);
        }
    }

    public static KieServicesClient getKieServerJmsClient(URL amqHost, Set<Class<?>> extraClasses) {
        try {
            return getKieServerJmsClient(amqHost, extraClasses, KIE_SERVER_TIMEOUT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Kie Server JMS Client.", e);
        }
    }

    public static KieServicesClient getKieServerJmsClient(URL amqHost, Set<Class<?>> extraClasses, long clientTimeout) throws Exception {
        ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory(
                "failover://(ssl://"+amqHost.getHost()+":443)?initialReconnectDelay=2000&maxReconnectAttempts=5");
        connectionFactory.setUserName(DeploymentConstants.getAmqUsername());
        connectionFactory.setPassword(DeploymentConstants.getAmqPassword());
        connectionFactory.setTrustStore(Paths.get(DeploymentConstants.getCertificateDir()+"/client.ts").toAbsolutePath().normalize().toString());
        connectionFactory.setTrustStorePassword("changeit");
        Queue sendQueue = null, receiveQueue = null;
        Connection c = null;
        try {
            c = connectionFactory.createConnection();
            c.start();
            Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            sendQueue = s.createQueue("queue/KIE.SERVER.REQUEST");
            receiveQueue = s.createQueue("queue/KIE.SERVER.RESPONSE");
        } finally {
            if (c != null) {
                c.close();
            }
        }
        KieServicesConfiguration kieServicesConfiguration = KieServicesFactory.newJMSConfiguration(connectionFactory, sendQueue, receiveQueue,
                DeploymentConstants.getAmqUsername(), DeploymentConstants.getAmqPassword());
        kieServicesConfiguration.setTimeout(clientTimeout);
        kieServicesConfiguration.addExtraClasses(extraClasses);
        kieServicesConfiguration.setMarshallingFormat(MarshallingFormat.JSON);

        return KieServicesFactory.newKieServicesClient(kieServicesConfiguration);
    }

    public static KieServicesClient getSmartRouterClient(SmartRouterDeployment smartRouterDeployment, String userName, String password) {
        return getSmartRouterClient(smartRouterDeployment, userName, password, KIE_SERVER_TIMEOUT);
    }

    public static KieServicesClient getSmartRouterClient(SmartRouterDeployment smartRouterDeployment, String userName, String password, long clientTimeout) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(smartRouterDeployment.getUrl().toString(),
                userName, password, clientTimeout);
        List<String> capabilities = Arrays.asList(KieServerConstants.CAPABILITY_BPM,
                KieServerConstants.CAPABILITY_BPM_UI,
                KieServerConstants.CAPABILITY_BRM,
                KieServerConstants.CAPABILITY_BRP,
                KieServerConstants.CAPABILITY_CASE,
                KieServerConstants.CAPABILITY_DMN);
        configuration.setCapabilities(capabilities);
        KieServicesClient kieServerClient = KieServicesFactory.newKieServicesClient(configuration);
        return kieServerClient;
    }

    public static ProcessServicesClient getProcessClient(KieServerDeployment kieServerDeployment) {
        return getKieServerClient(kieServerDeployment).getServicesClient(ProcessServicesClient.class);
    }
    public static ProcessServicesClient getProcessJmsClient(KieServicesClient kieServerJmsClient) {
        return kieServerJmsClient.getServicesClient(ProcessServicesClient.class);
    }

    public static UserTaskServicesClient getTaskClient(KieServerDeployment kieServerDeployment) {
        return getKieServerClient(kieServerDeployment).getServicesClient(UserTaskServicesClient.class);
    }
    public static UserTaskServicesClient getTaskJmsClient(KieServicesClient kieServerJmsClient) {
        return kieServerJmsClient.getServicesClient(UserTaskServicesClient.class);
    }

    public static QueryServicesClient getQueryClient(KieServerDeployment kieServerDeployment) {
        return getKieServerClient(kieServerDeployment).getServicesClient(QueryServicesClient.class);
    }
    public static QueryServicesClient getQueryJmsClient(KieServicesClient kieServerJmsClient) {
        return kieServerJmsClient.getServicesClient(QueryServicesClient.class);
    }

    public static RuleServicesClient getRuleClient(KieServerDeployment kieServerDeployment) {
        return getKieServerClient(kieServerDeployment).getServicesClient(RuleServicesClient.class);
    }
    public static RuleServicesClient getRuleJmsClient(KieServicesClient kieServerJmsClient) {
        return kieServerJmsClient.getServicesClient(RuleServicesClient.class);
    }

    public static SolverServicesClient getSolverClient(KieServerDeployment kieServerDeployment) {
        return getKieServerClient(kieServerDeployment).getServicesClient(SolverServicesClient.class);
    }
    public static SolverServicesClient getSolverJmsClient(KieServicesClient kieServerJmsClient) {
        return kieServerJmsClient.getServicesClient(SolverServicesClient.class);
    }

    public static void waitForContainerStart(KieServerDeployment kieServerDeployment, String containerId) {
        KieServicesClient kieServerClient = getKieServerClient(kieServerDeployment);

        Instant timeoutTime = Instant.now().plusSeconds(30);
        while (Instant.now().isBefore(timeoutTime)) {

            ServiceResponse<KieContainerResource> containerInfo = kieServerClient.getContainerInfo(containerId);
            boolean responseSuccess = containerInfo.getType().equals(ServiceResponse.ResponseType.SUCCESS);
            if(responseSuccess && containerInfo.getResult().getStatus().equals(KieContainerStatus.STARTED)) {
                return;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for pod to be ready.", e);
            }
        }
    }
}

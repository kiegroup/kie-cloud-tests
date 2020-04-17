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
package org.kie.cloud.integrationtests.integration;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.scenario.KieServerWithDatabaseScenario;
import org.kie.cloud.api.scenario.KjarDeploymentScenarioListener;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.TemplateNotSupported;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.client.util.Kjar;
import org.kie.processmigration.model.Execution;
import org.kie.processmigration.model.Migration;
import org.kie.processmigration.model.MigrationDefinition;
import org.kie.processmigration.model.Plan;
import org.kie.processmigration.model.ProcessRef;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category({JBPMOnly.class, TemplateNotSupported.class})
@RunWith(Parameterized.class)
public class ProcessInstanceMigrationIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieServerWithDatabaseScenario> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceMigrationIntegrationTest.class);

    @Parameter(value = 0)
    public String testScenarioName;

    @Parameter(value = 1)
    public KieServerWithDatabaseScenario kieServerScenario;

    private static final Kjar OLD_KJAR = Kjar.MIGRATION_PROJECT_100_SNAPSHOT;
    private static final String OLD_CONTAINER_ID = "migration-project-old";
    private static final Kjar NEW_KJAR = Kjar.MIGRATION_PROJECT_200_SNAPSHOT;
    private static final String NEW_CONTAINER_ID = "migration-project-new";
    private static final String KIE_CONTAINER_DEPLOYMENTS = OLD_CONTAINER_ID + "=" + OLD_KJAR.toString() + "|" + NEW_CONTAINER_ID + "=" + NEW_KJAR.toString();

    private static final String PROCESS_ID = "test.myprocess";

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    protected KieServicesClient kieServicesClient;
    protected ProcessServicesClient processServicesClient;
    protected String kieServerId;
    protected HttpClient client;
    protected List<Long> instancesIds;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

        try {
            KieServerWithDatabaseScenario kieServerMySqlScenario = deploymentScenarioFactory.getKieServerWithMySqlScenarioBuilder()
                                                                                            .withInternalMavenRepo(true)
                                                                                            .withContainerDeployment(KIE_CONTAINER_DEPLOYMENTS)
                                                                                            .withProcessMigrationDeployment()
                                                                                            .build();
            KjarDeploymentScenarioListener.addKjarDeployment(kieServerMySqlScenario, OLD_KJAR, NEW_KJAR);
            scenarios.add(new Object[]{"KIE Server + MySQL", kieServerMySqlScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server + MySQL is skipped.", ex);
        }

        try {
            KieServerWithDatabaseScenario kieServerPostgreSqlScenario = deploymentScenarioFactory.getKieServerWithPostgreSqlScenarioBuilder()
                                                                                                 .withInternalMavenRepo(true)
                                                                                                 .withContainerDeployment(KIE_CONTAINER_DEPLOYMENTS)
                                                                                                 .withProcessMigrationDeployment()
                                                                                                 .build();
            KjarDeploymentScenarioListener.addKjarDeployment(kieServerPostgreSqlScenario, OLD_KJAR, NEW_KJAR);
            scenarios.add(new Object[]{"KIE Server + PostgreSQL", kieServerPostgreSqlScenario});
        } catch (UnsupportedOperationException ex) {
            logger.info("KIE Server + PostgreSQL is skipped.", ex);
        }

        return scenarios;
    }

    @Override
    protected KieServerWithDatabaseScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return kieServerScenario;
    }

    @Before
    public void setUp() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        kieServicesClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        processServicesClient = KieServerClientProvider.getProcessClient(deploymentScenario.getKieServerDeployment());

        kieServerId = kieServicesClient.getServerInfo().getResult().getServerId();
        client = HttpClientBuilder.create().setDefaultCredentialsProvider(getBasicAuth()).build();
        instancesIds = new ArrayList<>();
    }

    @Test
    public void testProcessInstanceMigrationIntegration() throws IOException, JAXBException {
        // Given
        startProcesses();

        // When
        createMigration();

        // Then
        List<ProcessInstance> instances = processServicesClient.findProcessInstances(OLD_CONTAINER_ID, 0, 10);
        Assertions.assertThat(instances).hasSize(1);
        Assertions.assertThat(instances.get(0).getId()).isIn(instancesIds);

        instances = processServicesClient.findProcessInstances(NEW_CONTAINER_ID, 0, 10);
        Assertions.assertThat(instances).hasSize(1);
        Assertions.assertThat(instances.get(0).getId()).isIn(instancesIds);
    }

    private Migration createMigration() throws IOException, JAXBException {
        Plan plan = createPlan();
        MigrationDefinition def = new MigrationDefinition();
        def.setPlanId(plan.getId());
        def.setKieServerId(kieServerId);
        def.setProcessInstanceIds(Arrays.asList(instancesIds.get(0)));
        def.setExecution(new Execution().setType(Execution.ExecutionType.SYNC));

        HttpPost post = preparePost("/migrations");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, def);
        post.setEntity(new StringEntity(writer.toString()));
        HttpResponse r = client.execute(post);
        Assertions.assertThat(r.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        StringReader reader = new StringReader(EntityUtils.toString(r.getEntity()));
        Migration migration = mapper.readValue(reader, Migration.class);
        Assertions.assertThat(migration).isNotNull();
        Assertions.assertThat(migration.getDefinition().getRequester()).isEqualTo(deploymentScenario.getKieServerDeployment().getUsername());
        return migration;
    }

    private Plan createPlan() throws IOException, JAXBException {
        Plan plan = new Plan();
        plan.setSource(new ProcessRef().setContainerId(OLD_CONTAINER_ID).setProcessId(PROCESS_ID));
        plan.setTarget(new ProcessRef().setContainerId(NEW_CONTAINER_ID).setProcessId(PROCESS_ID));

        HttpPost post = preparePost("/plans");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, plan);
        post.setEntity(new StringEntity(writer.toString()));
        HttpResponse r = client.execute(post);
        Assertions.assertThat(r.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        StringReader reader = new StringReader(EntityUtils.toString(r.getEntity()));
        return mapper.readValue(reader, Plan.class);
    }

    private HttpPost preparePost(String path) {
        HttpPost post = new HttpPost(deploymentScenario.getProcessMigrationDeployment().getUrl()+"/rest" + path);
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return post;
    }

    private void startProcesses() {
        instancesIds.add(processServicesClient.startProcess(OLD_CONTAINER_ID, PROCESS_ID));
        instancesIds.add(processServicesClient.startProcess(OLD_CONTAINER_ID, PROCESS_ID));
    }


    private CredentialsProvider getBasicAuth() {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(deploymentScenario.getKieServerDeployment().getUsername(), deploymentScenario.getKieServerDeployment().getPassword()));
        return provider;
    }

}

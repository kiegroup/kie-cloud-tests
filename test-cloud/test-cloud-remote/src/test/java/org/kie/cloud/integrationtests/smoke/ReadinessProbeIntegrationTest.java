package org.kie.cloud.integrationtests.smoke;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.guvnor.rest.client.OrganizationalUnit;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.WorkbenchWithKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.common.provider.WorkbenchClientProvider;
import org.kie.cloud.git.GitProvider;
import org.kie.cloud.git.GitProviderFactory;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.shared.filter.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadinessProbeIntegrationTest {

    private static final String ORGANIZATION_UNIT_NAME = "myOrgUnit";
    private static final String REPOSITORY_NAME = "myRepo";

    private static final String PROJECT_GROUP_ID = "org.kie.server.testing";
    private static final String PROJECT_NAME = "definition-project";
    private static final String PROJECT_VERSION = "1.0.0.Final";

    private static final String CONTAINER_ID = "cont-id";
    private static final String CONTAINER_ALIAS = "cont-alias";

    private static final String GIT_REPOSITORY_NAME = "myGitRepo";

    private static final String WORKBENCH_LOGIN_SCREEN_TEXT = "Sign In";

    private DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactory.getInstance();
    private WorkbenchWithKieServerScenario workbenchWithKieServerScenario;
    private Client httpKieServerClient;
    private WorkbenchClientProvider workbenchClientProvider;
    private KieServerClientProvider kieServerClientProvider;
    private KieServerMgmtControllerClient kieControllerClient;

    private GitProvider gitProvider;

    private static final Logger logger = LoggerFactory.getLogger(ReadinessProbeIntegrationTest.class);

    @Before
    public void setUp() {
        workbenchWithKieServerScenario = deploymentScenarioFactory.getWorkbenchWithKieServerScenarioBuilder().build();
        workbenchWithKieServerScenario.deploy();

        workbenchClientProvider = new WorkbenchClientProvider(workbenchWithKieServerScenario.getWorkbenchDeployment());
        kieServerClientProvider = new KieServerClientProvider(workbenchWithKieServerScenario.getKieServerDeployment());

        gitProvider = GitProviderFactory.getGitProvider();
        gitProvider.createGitRepository(GIT_REPOSITORY_NAME, ClassLoader.class.getResource("/kjars-sources").getFile());

        workbenchClientProvider.createOrganizationalUnit(ORGANIZATION_UNIT_NAME, workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername());
        workbenchClientProvider.cloneRepository(ORGANIZATION_UNIT_NAME, REPOSITORY_NAME, gitProvider.getRepositoryUrl(GIT_REPOSITORY_NAME));
        workbenchClientProvider.deployProject(REPOSITORY_NAME, PROJECT_NAME);

        KieServerInfo serverInfo = kieServerClientProvider.getKieServerClient().getServerInfo().getResult();
        kieControllerClient = new KieServerMgmtControllerClient(workbenchWithKieServerScenario.getWorkbenchDeployment().getUrl().toString() + "/rest/controller",
                workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername(), workbenchWithKieServerScenario.getWorkbenchDeployment().getPassword());
        kieControllerClient.saveContainerSpec(serverInfo.getServerId(), serverInfo.getName(), CONTAINER_ID, CONTAINER_ALIAS, PROJECT_GROUP_ID, PROJECT_NAME, PROJECT_VERSION, KieContainerStatus.STARTED);

        httpKieServerClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(10, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .register(new Authenticator(workbenchWithKieServerScenario.getKieServerDeployment().getUsername(),
                        workbenchWithKieServerScenario.getKieServerDeployment().getPassword()))
                .build();
    }

    @After
    public void tearDown() {
        gitProvider.deleteGitRepository(GIT_REPOSITORY_NAME);
        workbenchWithKieServerScenario.undeploy();
    }

    @Test
    public void testWorkbenchReadinessProbe() {
        checkBCLoginScreenAvailable();
        logger.debug("Check that workbench REST is available");
        Collection<OrganizationalUnit> organizationalUnits = workbenchClientProvider.getWorkbenchClient().getOrganizationalUnits();
        Assertions.assertThat(organizationalUnits.stream().anyMatch(x -> x.getName().equals(ORGANIZATION_UNIT_NAME))).isTrue();

        logger.debug("Scale workbench to 0");
        workbenchWithKieServerScenario.getWorkbenchDeployment().scale(0);
        workbenchWithKieServerScenario.getWorkbenchDeployment().waitForScale();
        logger.debug("Scale workbench to 1");
        workbenchWithKieServerScenario.getWorkbenchDeployment().scale(1);
        workbenchWithKieServerScenario.getWorkbenchDeployment().waitForScale();

        checkBCLoginScreenAvailable();
        logger.debug("Check that workbench REST is available");
        workbenchClientProvider.createOrganizationalUnit(ORGANIZATION_UNIT_NAME, workbenchWithKieServerScenario.getWorkbenchDeployment().getUsername());
        organizationalUnits = workbenchClientProvider.getWorkbenchClient().getOrganizationalUnits();
        Assertions.assertThat(organizationalUnits.stream().anyMatch(x -> x.getName().equals(ORGANIZATION_UNIT_NAME))).isTrue();
    }

    @Test
    public void testKieServerReadinessProbe() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JAXB, this.getClass().getClassLoader());

        workbenchWithKieServerScenario.getKieServerDeployment().scale(0);
        workbenchWithKieServerScenario.getKieServerDeployment().waitForScale();
        workbenchWithKieServerScenario.getKieServerDeployment().scale(1);
        workbenchWithKieServerScenario.getKieServerDeployment().waitForScale();

        WebTarget target = httpKieServerClient.target(workbenchWithKieServerScenario.getKieServerDeployment().getUrl().toString() + "/services/rest/server/containers");

        long timeoutTime = Calendar.getInstance().getTimeInMillis() + 1000L;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            Response response = target.request().get();

            if(response.getStatus() == Response.Status.OK.getStatusCode()) {
                ServiceResponse<?> serviceResponse = marshaller.unmarshall(response.readEntity(String.class), ServiceResponse.class);
                KieContainerResourceList containerList = (KieContainerResourceList) serviceResponse.getResult();
                Assertions.assertThat(containerList.getContainers().size()).isEqualTo(1);
                Assertions.assertThat(containerList.getContainers().get(0).getContainerId()).isEqualTo(CONTAINER_ID);
                return;
            } else {
                response.close();
                Assertions.assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            }
        }
        Assertions.fail("Timeout while waiting for OpenShift router to establish connection to Kie server.");
    }

    private void checkBCLoginScreenAvailable() {
        logger.debug("Check that workbench login screen is available");
        URL url = workbenchWithKieServerScenario.getWorkbenchDeployment().getUrl();
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            logger.debug("Connecting to workbench login screen");
            httpURLConnection.connect();
            logger.debug("Http response code is {}", httpURLConnection.getResponseCode());
            Assertions.assertThat(httpURLConnection.getResponseCode()).isEqualTo(HttpURLConnection.HTTP_OK);

            logger.debug("Reading login page content");
            String responseContent = IOUtils.toString(httpURLConnection.getInputStream(), "UTF-8");
            logger.debug("Login page content contains {} characters", responseContent.length());
            httpURLConnection.disconnect();

            Assertions.assertThat(responseContent).contains(WORKBENCH_LOGIN_SCREEN_TEXT);

        } catch (IOException e) {
            Assertions.fail("Unable to load workbench login screen", e);
        }
    }
}

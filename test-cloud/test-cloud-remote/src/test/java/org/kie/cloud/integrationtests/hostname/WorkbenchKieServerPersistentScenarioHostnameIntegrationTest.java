package org.kie.cloud.integrationtests.hostname;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import cz.xtf.client.Http;
import cz.xtf.client.HttpResponseParser;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.DeploymentScenarioBuilderFactoryLoader;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieDeploymentScenario;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.integrationtests.category.TemplateNotSupported;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Category(TemplateNotSupported.class)
@RunWith(Parameterized.class)
public class WorkbenchKieServerPersistentScenarioHostnameIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<KieDeploymentScenario<?>> {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkbenchKieServerPersistentScenarioHostnameIntegrationTest.class);

    @Parameterized.Parameter(value = 0)
    public String testScenarioName;

    @Parameterized.Parameter(value = 1)
    public KieDeploymentScenario<?> workbenchKieServerScenario;

    @Parameterized.Parameter(value = 2)
    public boolean disabledSsl;

    private KieServerDeployment kieServerDeployment;
    private WorkbenchDeployment workbenchDeployment;

    private static final String SECURED_URL_PREFIX = "secured-";
    private static String randomUrlPrefix() {
        return UUID.randomUUID().toString().substring(0, 4) + "-";
    }

    private static final String WORKBENCH_NAME = "workbench";
    private static final String KIE_SERVER_NAME = "kieserver";
    private static final String WORKBENCH_HOSTNAME = WORKBENCH_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private static final String KIE_SERVER_INFO_REST_REQUEST_URL = "services/rest/server";

    private static final Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JAXB, WorkbenchKieServerPersistentScenarioHostnameIntegrationTest.class.getClassLoader());

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        List<Object[]> scenarios = new ArrayList<>();
        DeploymentScenarioBuilderFactory deploymentScenarioFactory = DeploymentScenarioBuilderFactoryLoader.getInstance();

            WorkbenchKieServerPersistentScenario httpHostnameWorkbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
            .withHttpKieServerHostname(randomUrlPrefix() + KIE_SERVER_HOSTNAME)
            .withHttpWorkbenchHostname(randomUrlPrefix() + WORKBENCH_HOSTNAME)
            .build();
            scenarios.add(new Object[] { "Workbench + KIE Server - custom HTTP route", httpHostnameWorkbenchKieServerPersistentScenario, true});

            WorkbenchKieServerPersistentScenario httpsHostnameWorkbenchKieServerPersistentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .withHttpsKieServerHostname(SECURED_URL_PREFIX + randomUrlPrefix() + KIE_SERVER_HOSTNAME)
                .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + randomUrlPrefix() + WORKBENCH_HOSTNAME)
                .build();
            scenarios.add(new Object[] { "Workbench + KIE Server - custom HTTPS route", httpsHostnameWorkbenchKieServerPersistentScenario, false });

        return scenarios;
    }

    @Override
    protected KieDeploymentScenario<?> createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return workbenchKieServerScenario;
    }

    @Before
    public void setUp() {
        workbenchDeployment = deploymentScenario.getWorkbenchDeployments().get(0);
        kieServerDeployment = deploymentScenario.getKieServerDeployments().get(0);
    }

    @Test
    public void simplyConfiguredCustomHostnameTest() {
        SoftAssertions softly = new SoftAssertions();
        if(disabledSsl) {
            softly.assertThat(workbenchDeployment.getInsecureUrl()).as("Workbench insecure URL").isNotEmpty().get().extracting(URL::getProtocol).isEqualTo("http");
            softly.assertThat(workbenchDeployment.getSecureUrl()).as("Workbench secure URL").isEmpty();
            softly.assertThat(kieServerDeployment.getInsecureUrl()).as("Kie Server insecure URL").isNotEmpty().get().extracting(URL::getProtocol).isEqualTo("http");
            softly.assertThat(kieServerDeployment.getSecureUrl()).as("Kie Server secure URL").isEmpty();
        } else {
            softly.assertThat(workbenchDeployment.getInsecureUrl()).as("Workbench insecure URL").isEmpty();
            softly.assertThat(workbenchDeployment.getSecureUrl()).as("Workbench secure URL").isNotEmpty().get().extracting(URL::getProtocol).isEqualTo("https");
            softly.assertThat(kieServerDeployment.getInsecureUrl()).as("Kie Server insecure URL").isEmpty();
            softly.assertThat(kieServerDeployment.getSecureUrl()).as("Kie Server secure URL").isNotEmpty().get().extracting(URL::getProtocol).isEqualTo("https");
        }
        softly.assertAll();

        //logger.debug("Test login screen on url {}", url.toString());
        String workbenchUrl = workbenchDeployment.getUrl().toString();
        try {
            logger.debug("Test login screen on url {}", workbenchUrl);
            HttpResponseParser responseAndCode = Http.get(workbenchUrl).trustAll().execute();
            assertThat(responseAndCode.code()).isEqualTo(HttpsURLConnection.HTTP_OK);
            assertThat(responseAndCode.response()).contains(WORKBENCH_LOGIN_SCREEN_TEXT);
        } catch (IOException e) {
            logger.error("Error in downloading workbench login screen using secure connection", e);
            fail("Error in downloading workbench login screen using secure connection", e);
        }

        String kieServerUrl = kieServerDeployment.getUrl().toString();
        try {
            logger.debug("Test Kie Server info on url {}", kieServerUrl);
            HttpResponseParser responseAndCode = Http.get(kieServerUrl +"/"+ KIE_SERVER_INFO_REST_REQUEST_URL)
                    .basicAuth(kieServerDeployment.getUsername(), kieServerDeployment.getPassword())
                    .preemptiveAuth()
                    .trustAll()
                    .execute();

            assertThat(responseAndCode.code()).isEqualTo(HttpsURLConnection.HTTP_OK);
            ServiceResponse<KieServerInfo> kieServerInfoServiceResponse = marshaller.unmarshall(responseAndCode.response(), ServiceResponse.class);
            KieServerInfo kieServerInfo = kieServerInfoServiceResponse.getResult();
            assertThat(kieServerInfo.getCapabilities()).contains(KieServerConstants.CAPABILITY_BRM);
        } catch (Exception e) {
            logger.error("Unable to connect to KIE server REST API", e);
            fail("Unable to connect to KIE server REST API", e);
        }

    }

}

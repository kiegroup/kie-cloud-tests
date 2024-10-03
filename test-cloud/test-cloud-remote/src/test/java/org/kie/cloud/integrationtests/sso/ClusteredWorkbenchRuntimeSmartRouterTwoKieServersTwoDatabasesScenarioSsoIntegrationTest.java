/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.integrationtests.sso;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario;
import org.kie.cloud.integrationtests.category.JBPMOnly;
import org.kie.cloud.integrationtests.category.MonitoringK8sFs;
import org.kie.cloud.integrationtests.testproviders.FireRulesTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.testproviders.ProcessTestProvider;
import org.kie.cloud.tests.common.AbstractCloudIntegrationTest;
import org.kie.cloud.tests.common.ScenarioDeployer;

@Category({JBPMOnly.class, MonitoringK8sFs.class}) // Smart router is available just for jBPM
public class ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioSsoIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String SECURED_URL_PREFIX = "secured-";
    private static final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private static final String BUSINESS_CENTRAL_NAME = "rhpamcentr";
    private static final String KIE_SERVER_NAME = "kieserver";

    private static final String BUSINESS_CENTRAL_HOSTNAME = BUSINESS_CENTRAL_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private static ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenario deploymentScenario;

    private static FireRulesTestProvider fireRulesTestProvider;
    private static ProcessTestProvider processTestProvider;
    private static HttpsKieServerTestProvider httpsKieServerTestProvider;
    private static HttpsWorkbenchTestProvider httpsWorkbenchTestProvider;

    @BeforeClass
    public static void initializeDeployment() {
        if (deploymentScenarioFactory.getCloudAPIImplementationName().equals("openshift-operator")) {
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                    .deploySso()
                    .withInternalMavenRepo()
                    .build();
        } else {
            deploymentScenario = deploymentScenarioFactory.getClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioBuilder()
                    .deploySso()
                    .withInternalMavenRepo()
                    .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + "mon-" + BUSINESS_CENTRAL_HOSTNAME)
                    .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + "mon-" + BUSINESS_CENTRAL_HOSTNAME)
                    .withHttpKieServer1Hostname(RANDOM_URL_PREFIX + "mon-0-" + KIE_SERVER_HOSTNAME)
                    .withHttpsKieServer1Hostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + "mon-0-" + KIE_SERVER_HOSTNAME)
                    .withHttpKieServer2Hostname(RANDOM_URL_PREFIX + "mon-1-" + KIE_SERVER_HOSTNAME)
                    .withHttpsKieServer2Hostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + "mon-1-" + KIE_SERVER_HOSTNAME)
                    .build();
        }
        deploymentScenario.setLogFolderName(ClusteredWorkbenchRuntimeSmartRouterTwoKieServersTwoDatabasesScenarioSsoIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);

        // Setup test providers
        fireRulesTestProvider = FireRulesTestProvider.create(deploymentScenario);
        processTestProvider = ProcessTestProvider.create(deploymentScenario);
        httpsKieServerTestProvider = HttpsKieServerTestProvider.create(deploymentScenario);
        httpsWorkbenchTestProvider = HttpsWorkbenchTestProvider.create();
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testRulesFromMavenRepo() {
        fireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerOneDeployment());
        fireRulesTestProvider.testDeployFromKieServerAndFireRules(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    @Category(JBPMOnly.class)
    public void testProcessFromMavenRepo() {
        processTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerOneDeployment());
        processTestProvider.testDeployFromKieServerAndExecuteProcesses(deploymentScenario.getKieServerTwoDeployment());
    }

    @Test
    public void testKieServerHttps() {
        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            httpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, true);
            httpsKieServerTestProvider.testDeployContainer(kieServerDeployment, true);
        }
    }

    @Test
    public void testWorkbenchHttps() {
        for (WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            httpsWorkbenchTestProvider.testLoginScreen(workbenchDeployment, true);
            httpsWorkbenchTestProvider.testControllerOperations(workbenchDeployment, true);
        }
    }
}

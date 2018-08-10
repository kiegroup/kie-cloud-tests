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

package org.kie.cloud.integrationtests.sso.https;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.WorkbenchDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerPersistentScenario;
import org.kie.cloud.integrationtests.AbstractCloudIntegrationTest;
import org.kie.cloud.integrationtests.testproviders.HttpsKieServerTestProvider;
import org.kie.cloud.integrationtests.testproviders.HttpsWorkbenchTestProvider;
import org.kie.cloud.integrationtests.util.ScenarioDeployer;
import org.kie.cloud.maven.constants.MavenConstants;

public class WorkbenchKieServerPersistentScenarioSsoHttpsIntegrationTest extends AbstractCloudIntegrationTest {

    private static final String SECURED_URL_PREFIX = "secured-";
    private static final String RANDOM_URL_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";

    private static final String BUSINESS_CENTRAL_NAME = "rhpamcentr";
    private static final String KIE_SERVER_NAME = "kieserver";

    private static final String BUSINESS_CENTRAL_HOSTNAME = BUSINESS_CENTRAL_NAME + DeploymentConstants.getDefaultDomainSuffix();
    private static final String KIE_SERVER_HOSTNAME = KIE_SERVER_NAME + DeploymentConstants.getDefaultDomainSuffix();

    private static WorkbenchKieServerPersistentScenario deploymentScenario;

    @BeforeClass
    public static void initializeDeployment() {
        deploymentScenario = deploymentScenarioFactory.getWorkbenchKieServerPersistentScenarioBuilder()
                .deploySso()
                .withExternalMavenRepo(MavenConstants.getMavenRepoUrl(), MavenConstants.getMavenRepoUser(), MavenConstants.getMavenRepoPassword())
                .withHttpWorkbenchHostname(RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpsWorkbenchHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + BUSINESS_CENTRAL_HOSTNAME)
                .withHttpKieServerHostname(RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .withHttpsKieServerHostname(SECURED_URL_PREFIX + RANDOM_URL_PREFIX + KIE_SERVER_HOSTNAME)
                .build();
        deploymentScenario.setLogFolderName(WorkbenchKieServerPersistentScenarioSsoHttpsIntegrationTest.class.getSimpleName());
        ScenarioDeployer.deployScenario(deploymentScenario);
    }

    @AfterClass
    public static void cleanEnvironment() {
        ScenarioDeployer.undeployScenario(deploymentScenario);
    }

    @Test
    public void testKieServerHttps() {
        for (KieServerDeployment kieServerDeployment : deploymentScenario.getKieServerDeployments()) {
            HttpsKieServerTestProvider.testKieServerInfo(kieServerDeployment, true);
            HttpsKieServerTestProvider.testDeployContainer(kieServerDeployment, true);
        }
    }

    @Test
    public void testWorkbenchHttps() {
        for (WorkbenchDeployment workbenchDeployment : deploymentScenario.getWorkbenchDeployments()) {
            HttpsWorkbenchTestProvider.testLoginScreen(workbenchDeployment, true);
            HttpsWorkbenchTestProvider.testControllerOperations(workbenchDeployment, true);
        }
    }
}

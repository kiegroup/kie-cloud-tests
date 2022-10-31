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

package org.kie.cloud.integrationtests.smoke;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.deployment.CommandExecutionResult;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.WorkbenchKieServerScenario;
import org.kie.cloud.common.provider.KieServerClientProvider;
import org.kie.cloud.integrationtests.category.Smoke;
import org.kie.cloud.tests.common.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.kie.server.client.KieServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Category(Smoke.class)
public class ImageVersionIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<WorkbenchKieServerScenario> {

    private static final Logger logger = LoggerFactory.getLogger(ImageVersionIntegrationTest.class);

    private static final String KIE_VERSION = DeploymentConstants.getKieArtifactVersion();
    private static final String KIE_API_ARTIFACT_NAME = "kie-api";
    private static final String DEPLOYMENT_PATH = "/opt/eap/standalone/deployments";
    private static final String ARTIFACT_VERSION_PATTERN_STRING = "(?:" + KIE_API_ARTIFACT_NAME + "-)(.*)\\.jar";
    private static final Pattern ARTIFACT_VERSION_PATTERN = Pattern.compile(ARTIFACT_VERSION_PATTERN_STRING);

    @Override
    protected WorkbenchKieServerScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getWorkbenchKieServerScenarioBuilder().build();
    }

    @Test
    public void testImageVersions() {
        assertSoftly(softly -> {
            softly.assertThat(getKieServerImageVersion()).isEqualTo(KIE_VERSION);
            softly.assertThat(getImageVersion(deploymentScenario.getWorkbenchDeployment()))
                  .as("Check that %s artifact is present with version %s", KIE_API_ARTIFACT_NAME, KIE_VERSION)
                  .isEqualTo(KIE_VERSION);
        });
    }

    private String getKieServerImageVersion() {
        KieServicesClient kieServerClient = KieServerClientProvider.getKieServerClient(deploymentScenario.getKieServerDeployment());
        return kieServerClient.getServerInfo().getResult().getVersion();
    }

    private String getImageVersion(Deployment deployment) {
        CommandExecutionResult checkVersionCommand = deployment.getInstances().get(0).runCommand("grep", "-r", KIE_API_ARTIFACT_NAME, DEPLOYMENT_PATH);

        if (!checkVersionCommand.getOutput().contains(KIE_VERSION)) {
            TimeUtils.wait(Duration.ofMinutes(90), Duration.ofSeconds(10), () -> {
                String output = deployment.getInstances().get(0).runCommand("grep", "-r", KIE_API_ARTIFACT_NAME, DEPLOYMENT_PATH).getOutput();
                logger.info("Try to get output from kie-app: " + output);
                return output.contains(KIE_VERSION);
            });
            checkVersionCommand = deployment.getInstances().get(0).runCommand("grep", "-r", KIE_API_ARTIFACT_NAME, DEPLOYMENT_PATH);
        }
        //TimeUtils.wait(Duration.ofMinutes(90), Duration.ofSeconds(10), () -> checkVersionCommand.getOutput().contains(KIE_VERSION));

        return getArtifactVersion(checkVersionCommand.getOutput());
    }

    private String getArtifactVersion(String resultOfArtifactSearch) {
        Matcher versionMatcher = ARTIFACT_VERSION_PATTERN.matcher(resultOfArtifactSearch);
        if (versionMatcher.find()) {
            return versionMatcher.group(1);
        }
        throw new RuntimeException("Result of artifact search '" + resultOfArtifactSearch + "' doesn't contain artifact version defined by regexp '" + ARTIFACT_VERSION_PATTERN_STRING);
    }
}

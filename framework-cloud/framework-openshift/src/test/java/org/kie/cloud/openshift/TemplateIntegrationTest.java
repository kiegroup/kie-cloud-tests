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

package org.kie.cloud.openshift;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.DeploymentConfig;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.openshift.resource.Project;

public class TemplateIntegrationTest extends OpenShiftIntegrationTestBase {

    private static final String BPMS_TEMPLATE_FILE = "bpms-template.json";
    private static final String DEPLOYMENT_CONFIG_NAME = "buscentr-myapp";

    private static final String KIE_ADMIN_USER_ENV_NAME = "KIE_ADMIN_USER";
    private static final String KIE_ADMIN_USER_ENV_DEFAULT_VALUE = "adminUser";
    private static final String KIE_ADMIN_USER_ENV_CUSTOM_VALUE = "customUser";
    private static final String KIE_ADMIN_PWD_ENV_NAME = "KIE_ADMIN_PWD";
    private static final String KIE_ADMIN_PWD_ENV_CUSTOM_VALUE = "myPWd";
    private static final String KIE_ADMIN_PWD_PATTERN = "[a-zA-Z]{6}[0-9]{1}!";

    private Project project;

    @Before
    public void createProject() {
        project = controller.createProject(projectName);
    }

    @After
    public void deleteProject() {
        project.delete();
    }

    @Test
    public void testTemplateProcessingCustomEnvironment() {
        HashMap<String, String> envVariables = new HashMap<>();
        envVariables.put(KIE_ADMIN_USER_ENV_NAME, KIE_ADMIN_USER_ENV_CUSTOM_VALUE);
        envVariables.put(KIE_ADMIN_PWD_ENV_NAME, KIE_ADMIN_PWD_ENV_CUSTOM_VALUE);
        project.processTemplateAndCreateResources(getTemplateUrl(), envVariables);

        Map<String, String> envMap = retrieveEnvironmentVariables();
        Assertions.assertThat(envMap).containsKeys(KIE_ADMIN_USER_ENV_NAME, KIE_ADMIN_PWD_ENV_NAME);
        Assertions.assertThat(envMap.get(KIE_ADMIN_USER_ENV_NAME)).isEqualTo(KIE_ADMIN_USER_ENV_CUSTOM_VALUE);
        Assertions.assertThat(envMap.get(KIE_ADMIN_PWD_ENV_NAME)).hasSize(5).containsPattern(Pattern.compile(KIE_ADMIN_PWD_ENV_CUSTOM_VALUE));
    }

    @Test
    public void testTemplateProcessingDefaultEnvironment() {
        project.processTemplateAndCreateResources(getTemplateUrl(), new HashMap<>());

        Map<String, String> envMap = retrieveEnvironmentVariables();
        Assertions.assertThat(envMap).containsKeys(KIE_ADMIN_USER_ENV_NAME, KIE_ADMIN_PWD_ENV_NAME);
        Assertions.assertThat(envMap.get(KIE_ADMIN_USER_ENV_NAME)).isEqualTo(KIE_ADMIN_USER_ENV_DEFAULT_VALUE);
        Assertions.assertThat(envMap.get(KIE_ADMIN_PWD_ENV_NAME)).hasSize(8).containsPattern(Pattern.compile(KIE_ADMIN_PWD_PATTERN));
    }

    private Map<String, String> retrieveEnvironmentVariables() {
        DeploymentConfig deploymentConfig = controller.getClient().deploymentConfigs().inNamespace(projectName).withName(DEPLOYMENT_CONFIG_NAME).get();
        List<Container> containers = deploymentConfig.getSpec().getTemplate().getSpec().getContainers();
        List<EnvVar> env = containers.get(0).getEnv();
        return env.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));
    }

    private String getTemplateUrl() {
        return TemplateIntegrationTest.class.getClassLoader().getResource(BPMS_TEMPLATE_FILE).toString();
    }
}

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

package org.kie.cloud.tests.template;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kie.cloud.api.scenario.MissingResourceException;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class TemplateParameterTest {

    private static final Logger log = LoggerFactory.getLogger(TemplateParameterTest.class);

    private OpenShiftClient openShiftClient;

    @Parameter(value = 0)
    public String openShiftTemplateName;

    @Parameter(value = 1)
    public OpenShiftTemplate openShiftTemplate;

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<>();
        for (OpenShiftTemplate openShiftTemplate : getAvailableOpenShiftTemplates()) {
            data.add(new Object[]{openShiftTemplate.toString(), openShiftTemplate});
        }
        return data;
    }

    @Before
    public void createOfflineOpenShiftClient() {
        OpenShiftConfig openShiftConfig = new OpenShiftConfigBuilder()
                .withDisableApiGroupCheck(true)
                .build();

        openShiftClient = new DefaultOpenShiftClient(openShiftConfig);
    }

    @Test
    public void testTemplateParameterDuplicities() {
        Template template = openShiftClient.templates().load(openShiftTemplate.getTemplateUrl()).get();

        assertThat(template.getParameters()).extracting(n -> n.getName()).doesNotHaveDuplicates();
    }

    @Test
    public void testTemplateParameterUsage() {
        Template template = openShiftClient.templates().load(openShiftTemplate.getTemplateUrl()).get();

        List<String> templatePropertyNameParameters = template.getParameters().stream()
                                                                              .map(n -> "${" + n.getName() + "}")
                                                                              .collect(Collectors.toList());
        assertThat(template.toString()).contains(templatePropertyNameParameters);
    }

    private static List<OpenShiftTemplate> getAvailableOpenShiftTemplates() {
        List<OpenShiftTemplate> availableOpenShiftTemplates = new ArrayList<>();

        for (OpenShiftTemplate template : OpenShiftTemplate.values()) {
            try {
                // Check if template URL is available.
                template.getTemplateUrl();
                availableOpenShiftTemplates.add(template);
            } catch (MissingResourceException e) {
                log.info("Template with name " + template.toString() + " is not available.");
            }
        }

        return availableOpenShiftTemplates;
    }
}

package org.kie.cloud.openshift.template;

import java.util.Properties;

import org.kie.cloud.openshift.util.PropertyLoader;

class OpenShiftTemplatePropertiesLoader {

    private OpenShiftTemplatePropertiesLoader() {
        // Util class
    }

    private static final Properties templateUrlProperties = new Properties();

    static {
        loadTemplatePropertiesFromResources();
    }

    static Properties getProperties() {
        return templateUrlProperties;
    }

    private static void loadTemplatePropertiesFromResources() {
        final ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        String secretConfigFile = projectProfile + "-app-secret.properties";
        String projectSpecificTemplate = String.format("templates-%s.properties", projectProfile);

        templateUrlProperties.putAll(PropertyLoader.loadProperties(OpenShiftTemplatePropertiesLoader.class, secretConfigFile));
        templateUrlProperties.putAll(PropertyLoader.loadProperties(OpenShiftTemplatePropertiesLoader.class, projectSpecificTemplate));

        // Load custom single testing templates
        templateUrlProperties.putAll(PropertyLoader.loadProperties(OpenShiftTemplatePropertiesLoader.class, "single-jbpm-templates.properties"));
    }
}

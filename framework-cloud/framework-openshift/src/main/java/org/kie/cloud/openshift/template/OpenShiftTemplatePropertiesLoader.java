package org.kie.cloud.openshift.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OpenShiftTemplatePropertiesLoader {

    private static final Logger log = LoggerFactory.getLogger(OpenShiftTemplatePropertiesLoader.class);

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

        addPropertiesFromResource(secretConfigFile);
        addPropertiesFromResource(projectSpecificTemplate);

        // Load custom single testing templates
        addPropertiesFromResource("single-jbpm-templates.properties");
    }

    private static void addPropertiesFromResource(String resourceFilename) {
        try (InputStream is = OpenShiftTemplatePropertiesLoader.class.getResourceAsStream(resourceFilename)) {
            if (is == null) {
                throw new NoSuchFileException(resourceFilename);
            }
            Properties properties = new Properties();
            properties.load(is);
            templateUrlProperties.putAll(properties);
            log.info("Loaded {} propert{} from {}",
                     properties.size(),
                     properties.size() == 1 ? "y" : "ies",
                     resourceFilename
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + resourceFilename, e);
        }
    }
}

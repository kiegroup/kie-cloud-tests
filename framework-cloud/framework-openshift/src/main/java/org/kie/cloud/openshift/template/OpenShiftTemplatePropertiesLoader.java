package org.kie.cloud.openshift.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OpenShiftTemplatePropertiesLoader {

    private static final Logger log = LoggerFactory.getLogger(OpenShiftTemplatePropertiesLoader.class);

    private static Properties templateUrlProperties;

    static Properties load() {
        templateUrlProperties = new Properties();
        loadTemplatePropertiesFromResources();
        return templateUrlProperties;
    }

    private static void loadTemplatePropertiesFromResources() {
        final TemplateSelector.Product product = TemplateSelector.getProduct();
        final TemplateSelector.Database database = TemplateSelector.getDatabase();

        String secretConfigFile = product.name() + "-app-secret.properties";
        String dbSpecificTemplates = String.format("templates-%s-%s.properties", product.name(), database.name());

        addPropertiesFromResource(secretConfigFile);
        addPropertiesFromResource(dbSpecificTemplates);

        // TODO ugly - we ALWAYS have to load general templates, even if DB is postgres/mysql
        if (database != TemplateSelector.Database.general) {
            String generalTemplates = String.format("templates-%s-%s.properties", product.name(), TemplateSelector.Database.general);
            addPropertiesFromResource(generalTemplates);
        }

        // TODO uncomment when Jakub's PR merged
        //addPropertiesFromResource("single-jbpm-templates.properties");
    }

    private static void addPropertiesFromResource(String resourceFilename) {
        try (InputStream is = OpenShiftTemplatePropertiesLoader.class.getResourceAsStream(resourceFilename)) {
            Properties properties = new Properties();
            properties.load(is);
            templateUrlProperties.putAll(properties);
            log.info("Loaded {} properties from {}", properties.size(), resourceFilename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from " + resourceFilename, e);
        }
    }
}

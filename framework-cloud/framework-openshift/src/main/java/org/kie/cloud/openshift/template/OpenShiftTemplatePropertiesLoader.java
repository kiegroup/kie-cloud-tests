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
        final TemplateSelector.Project product = TemplateSelector.getProject();
        final TemplateSelector.Database database = TemplateSelector.getDatabase();

        String secretConfigFile = product + "-app-secret.properties";
        String dbSpecificTemplates = String.format("templates-%s-%s.properties", product, database);

        addPropertiesFromResource(secretConfigFile);
        addPropertiesFromResource(dbSpecificTemplates);

        // TODO ugly - we ALWAYS have to load general templates, even if DB is postgres/mysql
        if (database != TemplateSelector.Database.GENERAL) {
            String generalTemplates = String.format("templates-%s-%s.properties", product, TemplateSelector.Database.GENERAL);
            addPropertiesFromResource(generalTemplates);
        }

        // TODO uncomment when Jakub's PR merged
        //addPropertiesFromResource("single-jbpm-templates.properties");
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

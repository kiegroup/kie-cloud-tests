package org.kie.cloud.utils;

import java.util.Properties;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.kie.cloud.openshift.template.ProjectProfile;
import org.kie.cloud.openshift.util.PropertyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunnerFeature extends TestWatcher {
	private static final Logger LOG = LoggerFactory.getLogger(TestRunnerFeature.class);
	private static final String COMMON_PROPERTIES = "test.properties";
    private static final String TEMPLATE_SOURCES_FILE = "templates-%s.properties";

	private String testPropertiesFile;
	private final ThreadLocal<Properties> previousPropsBag = new ThreadLocal<>();

	public TestRunnerFeature(String testPropertiesFile) {
		fromResources(COMMON_PROPERTIES);
		this.testPropertiesFile = testPropertiesFile;
	}

	@Override
	protected void starting(Description description) {
		previousPropsBag.set(System.getProperties());
		fromResources(testPropertiesFile);
        fromSourcesRecursively(ProjectProfile.class, defaultTemplateFile());
	}

    private String defaultTemplateFile() {
        return String.format(TEMPLATE_SOURCES_FILE, System.getProperty("template.project"));
    }

	@Override
	protected void finished(Description description) {
		Properties previousProps = previousPropsBag.get();
		if (previousProps != null) {
			System.setProperties(previousProps);
		}
	}

    private static final void fromSourcesRecursively(Class<?> sources, String file) {
        injectProperties(PropertyLoader.loadProperties(sources, file));
    }

    private static final void fromResources(String file) {
        
        Properties prop = new Properties();
        try {
            prop.load(TestRunnerFeature.class.getClassLoader().getResourceAsStream(file));
            injectProperties(prop);
        } catch (Exception e) {
            LOG.warn("Could not load properties for runner", e);
        }
    }

    private static final void injectProperties(Properties prop) {
        System.getProperties().putAll(prop);
    }
}

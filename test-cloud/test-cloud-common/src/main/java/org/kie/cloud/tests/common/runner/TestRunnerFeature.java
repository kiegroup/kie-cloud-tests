package org.kie.cloud.tests.common.runner;

import java.io.IOException;
import java.util.Properties;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunnerFeature extends TestWatcher {
	private static final Logger LOG = LoggerFactory.getLogger(TestRunnerFeature.class);
	private static final String COMMON_PROPERTIES = "test.properties";

	private String testPropertiesFile;
	private final ThreadLocal<Properties> previousPropsBag = new ThreadLocal<>();
	
	public TestRunnerFeature(String testPropertiesFile) {
		loadProperties(COMMON_PROPERTIES);
		this.testPropertiesFile = testPropertiesFile;
	}

	@Override
	protected void starting(Description description) {
		previousPropsBag.set(System.getProperties());
		loadProperties(testPropertiesFile);
	}

	@Override
	protected void finished(Description description) {
		Properties previousProps = previousPropsBag.get();
		if (previousProps != null) {
			System.setProperties(previousProps);
		}
	}

	private static final void loadProperties(String file) {
		Properties prop = new Properties();
		try {
			prop.load(TestRunnerFeature.class.getClassLoader().getResourceAsStream(file));
			System.getProperties().putAll(prop);
		} catch (IOException e) {
			LOG.warn("Could not load properties for runner", e);
		}
	}
}

package org.kie.cloud.integrationtests.optaweb;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.kie.cloud.api.DeploymentScenarioBuilderFactory;
import org.kie.cloud.api.scenario.EmployeeRosteringScenario;
import org.kie.cloud.integrationtests.AbstractMethodIsolatedCloudIntegrationTest;
import org.kie.cloud.integrationtests.testproviders.OptawebEmployeeRosteringTestProvider;

public class OptawebEmployeeRosteringIntegrationTest extends AbstractMethodIsolatedCloudIntegrationTest<EmployeeRosteringScenario> {

    private OptawebEmployeeRosteringTestProvider testProvider;

    @Override
    protected EmployeeRosteringScenario createDeploymentScenario(DeploymentScenarioBuilderFactory deploymentScenarioFactory) {
        return deploymentScenarioFactory.getEmployeeRosteringScenarioBuilder().build();
    }

    @Before
    public void setup() {
        testProvider = new OptawebEmployeeRosteringTestProvider(getSanitizedUrl());
    }

    @Test
    public void fromSkillToRoster() {
        testProvider.fromSkillToRoster();
    }

    private URL getSanitizedUrl() {
        URL appUrl = deploymentScenario.getEmployeeRosteringDeployment().getUrl();
        String appUrlString = appUrl.toExternalForm();
        String sanitizedAppUrl = appUrlString.endsWith("/") ? appUrlString : appUrlString + "/";
        try {
            return new URL(sanitizedAppUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Optaweb Employee Rostering application URL.", e);
        }
    }
}

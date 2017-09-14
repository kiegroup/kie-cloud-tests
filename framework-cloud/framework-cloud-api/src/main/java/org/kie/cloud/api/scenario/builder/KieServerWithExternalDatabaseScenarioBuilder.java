package org.kie.cloud.api.scenario.builder;

import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;

public interface KieServerWithExternalDatabaseScenarioBuilder extends DeploymentScenarioBuilder<KieServerWithExternalDatabaseScenario> {
    public KieServerWithExternalDatabaseScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword);
}

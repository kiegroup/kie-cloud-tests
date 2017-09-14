package org.kie.cloud.openshift.scenario.builder;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.api.scenario.builder.KieServerWithExternalDatabaseScenarioBuilder;
import org.kie.cloud.api.scenario.builder.WorkbenchWithKieServerScenarioBuilder;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.scenario.KieServerWithExternalDatabaseScenarioImpl;

public class KieServerWithExternalDatabaseScenarioBuilderImpl implements KieServerWithExternalDatabaseScenarioBuilder {

    private OpenShiftController openshiftController;
    private Map<String, String> envVariables;

    public KieServerWithExternalDatabaseScenarioBuilderImpl(OpenShiftController openShiftController) {
        this.openshiftController = openShiftController;

        this.envVariables = new HashMap<String, String>();
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_USER, DeploymentConstants.getKieServerUser());
        this.envVariables.put(OpenShiftTemplateConstants.KIE_SERVER_PWD, DeploymentConstants.getKieServerPassword());

        this.envVariables.put("DB_HOST", System.getProperty("db.hostname"));
        this.envVariables.put("DB_DATABASE", System.getProperty("db.name"));
        this.envVariables.put("DB_USERNAME", System.getProperty("db.username"));
        this.envVariables.put("DB_PASSWORD", System.getProperty("db.password"));
    }

    @Override public KieServerWithExternalDatabaseScenario build() {
        return new KieServerWithExternalDatabaseScenarioImpl(openshiftController, envVariables);
    }

    @Override
    public KieServerWithExternalDatabaseScenarioBuilder withExternalMavenRepo(String repoUrl, String repoUserName, String repoPassword) {
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, repoUrl);
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, repoUserName);
        this.envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, repoPassword);
        return this;
    }
}

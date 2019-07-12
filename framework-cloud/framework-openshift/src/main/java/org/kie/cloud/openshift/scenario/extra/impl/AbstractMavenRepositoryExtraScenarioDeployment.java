package org.kie.cloud.openshift.scenario.extra.impl;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.extra.AbstractExtraScenarionDeployment;
import org.kie.cloud.openshift.scenario.extra.MavenRepositoryExtraScenarioDeployment;
import org.kie.cloud.openshift.util.MavenRepositoryDeployer;

public abstract class AbstractMavenRepositoryExtraScenarioDeployment<U> extends AbstractExtraScenarionDeployment<MavenRepositoryDeployment, U> implements MavenRepositoryExtraScenarioDeployment<U> {

    public AbstractMavenRepositoryExtraScenarioDeployment(Map<String, String> config) {
        super(config);
    }

    @Override
    protected MavenRepositoryDeployment deployToProject(Project project) {
        return MavenRepositoryDeployer.deploy(project, false);
    }

    @Override
    public Map<String, String> getDeploymentVariables() {
        Map<String, String> variables = new HashMap<>();
        variables.put(MAVEN_SNAPSHOT_URL, getDeploymentInformation().getSnapshotsRepositoryUrl().toString());
        variables.put(MAVEN_RELEASE_URL, getDeploymentInformation().getReleasesRepositoryUrl().toString());
        variables.put(MAVEN_REPO_USERNAME, "admin");
        variables.put(MAVEN_REPO_PASSWORD, "admin123");
        return variables;
    }

}

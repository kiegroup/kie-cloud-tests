package org.kie.cloud.openshift.deployment.external.impl;

import java.util.HashMap;
import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.deployment.external.AbstractExternalDeployment;
import org.kie.cloud.openshift.deployment.external.MavenRepositoryExternalDeployment;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.MavenRepositoryDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMavenRepositoryExternalDeployment<U> extends AbstractExternalDeployment<MavenRepositoryDeployment, U> implements MavenRepositoryExternalDeployment<U> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMavenRepositoryExternalDeployment.class);

    public AbstractMavenRepositoryExternalDeployment(Map<String, String> config) {
        super(config);
    }

    @Override
    protected MavenRepositoryDeployment deployToProject(Project project) {
        logger.info("Maven deployToProject for deplyment {}", this.getKey());
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

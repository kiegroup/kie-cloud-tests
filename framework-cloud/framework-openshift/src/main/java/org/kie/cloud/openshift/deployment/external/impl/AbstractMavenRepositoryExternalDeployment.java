package org.kie.cloud.openshift.deployment.external.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.deployment.external.AbstractExternalDeployment;
import org.kie.cloud.openshift.deployment.external.MavenRepositoryExternalDeployment;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.util.MavenRepositoryDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMavenRepositoryExternalDeployment<U> extends AbstractExternalDeployment<MavenRepositoryDeployment, U> implements MavenRepositoryExternalDeployment<U> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMavenRepositoryExternalDeployment.class);

    protected static final String SYSTEM_MAVEN_REPO_URL = "maven.repo.url";
    protected static final String SYSTEM_MAVEN_REPO_USERNAME = "maven.repo.username";
    protected static final String SYSTEM_MAVEN_REPO_PASSWORD = "maven.repo.password";

    protected Map<String, String> oldValues = new HashMap<String, String>();

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

    @Override
    public void configure(U obj) {
        // TODO to change once not using system properties anymore for kjars ...
        // Save old configuration
        saveSystemProperty(SYSTEM_MAVEN_REPO_URL);
        saveSystemProperty(SYSTEM_MAVEN_REPO_USERNAME);
        saveSystemProperty(SYSTEM_MAVEN_REPO_PASSWORD);

        // Setup new system properties
        MavenRepositoryDeployment deployment = getDeploymentInformation();
        System.setProperty(SYSTEM_MAVEN_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString());
        System.setProperty(SYSTEM_MAVEN_REPO_USERNAME, deployment.getUsername());
        System.setProperty(SYSTEM_MAVEN_REPO_PASSWORD, deployment.getPassword());
    }

    @Override
    public void removeConfiguration(U object) {
        // Restore system properties
        restoreSystemProperty(SYSTEM_MAVEN_REPO_URL);
        restoreSystemProperty(SYSTEM_MAVEN_REPO_USERNAME);
        restoreSystemProperty(SYSTEM_MAVEN_REPO_PASSWORD);
    }

    private void saveSystemProperty(String key) {
        oldValues.put(key, System.getProperty(key));
    }

    private void restoreSystemProperty(String key) {
        Optional.ofNullable(oldValues.get(key)).ifPresent(value -> System.setProperty(key, value));
    }
}

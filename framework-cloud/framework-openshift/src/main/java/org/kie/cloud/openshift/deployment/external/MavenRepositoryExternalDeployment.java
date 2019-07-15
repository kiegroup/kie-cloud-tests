package org.kie.cloud.openshift.deployment.external;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;

public interface MavenRepositoryExternalDeployment<U> extends ExternalDeployment<MavenRepositoryDeployment, U> {

    static String ID = "MavenRepositoryExtraScenarioDeployment";

    static String MAVEN_SNAPSHOT_URL = "MAVEN_SNAPSHOT_URL";
    static String MAVEN_RELEASE_URL = "MAVEN_RELEASE_URL";
    static String MAVEN_REPO_USERNAME = "MAVEN_REPO_USERNAME";
    static String MAVEN_REPO_PASSWORD = "MAVEN_REPO_PASSWORD";

    default String getKey() {
        return ID;
    }
}

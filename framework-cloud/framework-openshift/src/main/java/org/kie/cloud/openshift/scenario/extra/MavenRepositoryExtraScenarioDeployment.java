package org.kie.cloud.openshift.scenario.extra;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;

public interface MavenRepositoryExtraScenarioDeployment<U> extends ExtraScenarioDeployment<MavenRepositoryDeployment, U> {

    static String ID = "MavenRepositoryExtraScenarioDeployment";

    static String MAVEN_SNAPSHOT_URL = "MAVEN_SNAPSHOT_URL";
    static String MAVEN_RELEASE_URL = "MAVEN_RELEASE_URL";
    static String MAVEN_REPO_USERNAME = "MAVEN_REPO_USERNAME";
    static String MAVEN_REPO_PASSWORD = "MAVEN_REPO_PASSWORD";

    default String getKey() {
        return ID;
    }
}

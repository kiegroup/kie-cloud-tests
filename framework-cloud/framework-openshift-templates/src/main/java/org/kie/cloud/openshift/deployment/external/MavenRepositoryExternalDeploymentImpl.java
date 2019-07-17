package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.external.impl.AbstractMavenRepositoryExternalDeployment;

public class MavenRepositoryExternalDeploymentImpl extends AbstractMavenRepositoryExternalDeployment<Map<String, String>> implements ExternalDeploymentTemplates<MavenRepositoryDeployment> {

    public MavenRepositoryExternalDeploymentImpl(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(Map<String, String> envVariables) {
        MavenRepositoryDeployment deployment = getDeploymentInformation();
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString());
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_USERNAME, deployment.getUsername());
        envVariables.put(OpenShiftTemplateConstants.MAVEN_REPO_PASSWORD, deployment.getPassword());

        // TODO to change ...
        System.setProperty("maven.repo.url", deployment.getSnapshotsRepositoryUrl().toString());
        System.setProperty("maven.repo.username", deployment.getUsername());
        System.setProperty("maven.repo.password", deployment.getPassword());
    }

}

package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.constants.OpenShiftApbConstants;
import org.kie.cloud.openshift.deployment.external.impl.AbstractMavenRepositoryExternalDeployment;

public class MavenRepositoryExternalDeploymentApb extends AbstractMavenRepositoryExternalDeployment<Map<String, String>> implements ExternalDeploymentApb<MavenRepositoryDeployment> {

    public MavenRepositoryExternalDeploymentApb(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(Map<String, String> extraVars) {
        super.configure(extraVars);

        MavenRepositoryDeployment deployment = getDeploymentInformation();
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_URL, deployment.getSnapshotsRepositoryUrl().toString());
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_USER, deployment.getUsername());
        extraVars.put(OpenShiftApbConstants.MAVEN_REPO_PWD, deployment.getPassword());
    }

}

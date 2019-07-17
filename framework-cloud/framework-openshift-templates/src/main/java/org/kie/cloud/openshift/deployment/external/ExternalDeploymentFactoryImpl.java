package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeployment;
import org.kie.cloud.openshift.deployment.external.ExternalDeploymentFactory;
import org.kie.cloud.openshift.deployment.external.MavenRepositoryExternalDeployment;

public class ExternalDeploymentFactoryImpl implements ExternalDeploymentFactory {

    public ExternalDeployment<? extends Deployment, ?> get(String extraDeploymentKey, Map<String, String> extraDeploymentConfig) {
        switch (extraDeploymentKey) {
            case MavenRepositoryExternalDeployment.ID:
                return new MavenRepositoryExternalDeploymentImpl(extraDeploymentConfig);
            default:
                throw new RuntimeException("Unknown extra scenario deployment: " + extraDeploymentKey);
        }
    }
}
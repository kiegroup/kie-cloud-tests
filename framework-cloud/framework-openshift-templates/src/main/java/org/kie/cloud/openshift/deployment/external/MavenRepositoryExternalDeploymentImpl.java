package org.kie.cloud.openshift.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.deployment.external.impl.AbstractMavenRepositoryExternalDeployment;

public class MavenRepositoryExternalDeploymentImpl extends AbstractMavenRepositoryExternalDeployment<Map<String, String>> implements ExternalDeploymentTemplates<MavenRepositoryDeployment> {

    public MavenRepositoryExternalDeploymentImpl(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(Map<String, String> object) {
        // TODO Auto-generated method stub
    }

}

package org.kie.cloud.openshift.operator.deployment.external;

import java.util.Map;

import org.kie.cloud.api.deployment.MavenRepositoryDeployment;
import org.kie.cloud.openshift.deployment.external.impl.AbstractMavenRepositoryExternalDeployment;
import org.kie.cloud.openshift.operator.model.KieApp;

public class MavenRepositoryExternalDeploymentImpl extends AbstractMavenRepositoryExternalDeployment<KieApp> implements ExternalDeploymentOperator<MavenRepositoryDeployment> {

    public MavenRepositoryExternalDeploymentImpl(Map<String, String> config) {
        super(config);
    }

    @Override
    public void configure(KieApp kieApp) {
        // TODO Auto-generated method stub
    }

}

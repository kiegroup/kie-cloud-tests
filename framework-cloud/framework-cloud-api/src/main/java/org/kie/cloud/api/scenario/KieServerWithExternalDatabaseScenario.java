package org.kie.cloud.api.scenario;

import org.kie.cloud.api.deployment.KieServerDeployment;

/**
 * Representation of deployment scenario with Kie server and external database.
 */
public interface KieServerWithExternalDatabaseScenario extends DeploymentScenario {

    /**
     * Return Kie Server deployment.
     *
     * @return KieServerDeployment
     * @see KieServerDeployment
     */
    KieServerDeployment getKieServerDeployment();
}

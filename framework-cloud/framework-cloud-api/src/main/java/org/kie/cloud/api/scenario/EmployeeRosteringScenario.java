package org.kie.cloud.api.scenario;

import org.kie.cloud.api.deployment.EmployeeRosteringDeployment;

public interface EmployeeRosteringScenario extends DeploymentScenario {

    EmployeeRosteringDeployment getEmployeeRosteringDeployment();
}

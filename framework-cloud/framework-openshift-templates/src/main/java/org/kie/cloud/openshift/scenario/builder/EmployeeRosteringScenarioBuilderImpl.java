package org.kie.cloud.openshift.scenario.builder;

import org.kie.cloud.api.scenario.EmployeeRosteringScenario;
import org.kie.cloud.api.scenario.builder.EmployeeRosteringScenarioBuilder;
import org.kie.cloud.openshift.scenario.EmployeeRosteringScenarioImpl;

public class EmployeeRosteringScenarioBuilderImpl implements EmployeeRosteringScenarioBuilder {

    @Override
    public EmployeeRosteringScenario build() {
        return new EmployeeRosteringScenarioImpl();
    }
}

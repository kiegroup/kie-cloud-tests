package org.kie.cloud.openshift.scenario;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.EmployeeRosteringDeployment;
import org.kie.cloud.api.scenario.EmployeeRosteringScenario;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.EmployeeRosteringDeploymentImpl;
import org.kie.cloud.openshift.template.OpenShiftTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeRosteringScenarioImpl extends OpenShiftScenario implements EmployeeRosteringScenario {

    private static final String OPTAWEB_HTTPS_SECRET = "OPTAWEB_HTTPS_SECRET";

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeRosteringScenarioImpl.class);

    private EmployeeRosteringDeployment employeeRosteringDeployment;

    @Override
    public void deploy() {
        super.deploy();

        this.employeeRosteringDeployment = new EmployeeRosteringDeploymentImpl(project);

        Map<String, String> env = new HashMap<>();
        env.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, project.getName());
        env.put(OPTAWEB_HTTPS_SECRET, OpenShiftConstants.getKieApplicationSecretName());

        project.processTemplateAndCreateResources(OpenShiftTemplate.OPTAWEB_EMPLOYEE_ROSTERING.getTemplateUrl(), env);

        LOGGER.info("Waiting for OptaWeb Employee Rostering deployment to become ready.");
        employeeRosteringDeployment.waitForScale();
        LOGGER.info("Waiting for OptaWeb Employee Rostering has been deployed.");
    }

    @Override
    public List<Deployment> getDeployments() {
        return Collections.singletonList(employeeRosteringDeployment);
    }

    public EmployeeRosteringDeployment getEmployeeRosteringDeployment() {
        return this.employeeRosteringDeployment;
    }
}

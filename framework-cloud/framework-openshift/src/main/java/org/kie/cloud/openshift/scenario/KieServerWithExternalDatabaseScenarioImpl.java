package org.kie.cloud.openshift.scenario;

import static org.kie.cloud.openshift.scenario.util.ProjectUtils.createProject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.api.deployment.constants.DeploymentConstants;
import org.kie.cloud.api.scenario.KieServerWithExternalDatabaseScenario;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.constants.OpenShiftTemplateConstants;
import org.kie.cloud.openshift.deployment.KieServerDeploymentImpl;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerWithExternalDatabaseScenarioImpl implements KieServerWithExternalDatabaseScenario {

    private OpenShiftController openshiftController;
    private KieServerDeploymentImpl kieServerDeployment;
    private Project project;
    private Map<String, String> envVariables;

    private static final Logger logger = LoggerFactory.getLogger(KieServerWithExternalDatabaseScenario.class);

    public KieServerWithExternalDatabaseScenarioImpl(OpenShiftController openShiftController, Map<String, String> envVariables) {
        this.openshiftController = openShiftController;
        this.envVariables = envVariables;
    }

    @Override public KieServerDeployment getKieServerDeployment() {
        return kieServerDeployment;
    }

    @Override public String getNamespace() {
        return project.getName();
    }

    @Override public void deploy() {
        project = createProject(openshiftController);

        logger.info("Processing template and creating resources from " + OpenShiftConstants.getKieAppTemplateWorkbenchKieServerDatabase());
        envVariables.put(OpenShiftTemplateConstants.IMAGE_STREAM_NAMESPACE, project.getName());
        project.processTemplateAndCreateResources(OpenShiftConstants.getKieAppTemplateKieServerDatabaseExternal(), envVariables);

        kieServerDeployment = new KieServerDeploymentImpl();
        kieServerDeployment.setOpenShiftController(openshiftController);
        kieServerDeployment.setNamespace(project.getName());
        kieServerDeployment.setUsername(DeploymentConstants.getKieServerUser());
        kieServerDeployment.setPassword(DeploymentConstants.getKieServerPassword());
        kieServerDeployment.setServiceName(OpenShiftConstants.getKieApplicationName());
        kieServerDeployment.setSecureServiceName(OpenShiftConstants.getKieApplicationName());

        logger.info("Waiting for Kie server deployment to become ready.");
        kieServerDeployment.waitForScale();
    }

    @Override public void undeploy() {
        InstanceLogUtil.writeDeploymentLogs(this);

        for(Deployment deployment : getDeployments()) {
            if(deployment != null) {
                deployment.scale(0);
                deployment.waitForScale();
            }
        }

        project.delete();
    }

    public OpenShiftController getOpenshiftController() {
        return openshiftController;
    }

    @Override public List<Deployment> getDeployments() {
        return Arrays.asList(kieServerDeployment);
    }
}

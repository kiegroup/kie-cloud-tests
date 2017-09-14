package org.kie.cloud.openshift.scenario.util;

import java.util.UUID;

import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProjectUtils.class);

    public static Project createProject(OpenShiftController openshiftController) {
        String projectName = UUID.randomUUID().toString().substring(0, 4);
        if (OpenShiftConstants.getNamespacePrefix().isPresent()) {
            projectName = OpenShiftConstants.getNamespacePrefix().get() + "-" + projectName;
        }

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        Project project = openshiftController.createProject(projectName);

        logger.info("Creating secrets from " + OpenShiftConstants.getKieAppSecret());
        project.createResources(OpenShiftConstants.getKieAppSecret());

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        return project;
    }
}

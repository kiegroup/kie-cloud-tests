/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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

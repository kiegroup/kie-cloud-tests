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

package org.kie.cloud.images;

import java.time.Duration;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.kie.cloud.common.time.TimeUtils;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.image.ImageStream;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTestBase {

    private static final Logger logger = LoggerFactory.getLogger(ImageTestBase.class);

    protected OpenShiftController controller;
    protected Project project;
    protected String projectName;

    @Before
    public void prepareProject() {
        controller = new OpenShiftController(OpenShiftConstants.getOpenShiftUrl(),
                OpenShiftConstants.getOpenShiftUserName(),
                OpenShiftConstants.getOpenShiftPassword());

        projectName = UUID.randomUUID().toString().substring(0, 4);
        OpenShiftConstants.getNamespacePrefix().ifPresent(p -> projectName = p + "-" + projectName);

        logger.info("Generated project name is " + projectName);

        logger.info("Creating project " + projectName);
        project = controller.createProject(projectName);

        logger.info("Creating image streams from " + OpenShiftConstants.getKieImageStreams());
        project.createResources(OpenShiftConstants.getKieImageStreams());

        // Wait until image streams are available
        for (ImageStream imageStream : ImageStream.values()) {
            TimeUtils.wait(Duration.ofSeconds(10), Duration.ofMillis(200), () -> {
                return project.getImage(imageStream) != null;
            });
        }
    }

    @After
    public void cleanupProject() {
        project.delete();
        controller.close();
    }
}

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

package org.kie.cloud.openshift;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenShiftIntegrationTestBase {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftIntegrationTestBase.class);

    private static final String OPENSHIFT_URL = "https://localhost:8443";
    private static final String OPENSHIFT_USER = "user";
    private static final String OPENSHIFT_PASSWORD = "redhat";

    protected static OpenShiftController controller;

    protected String projectName;

    @BeforeClass
    public static void initializeController() {
        controller = new OpenShiftController(OPENSHIFT_URL, OPENSHIFT_USER, OPENSHIFT_PASSWORD);
    }

    @Before
    public void generateProjectName() {
        projectName = UUID.randomUUID().toString();
        logger.info("Generated project name is {}.", projectName);
    }

    @AfterClass
    public static void destroyController() {
        controller.close();
    }
}

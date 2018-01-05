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

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import org.kie.cloud.openshift.constants.OpenShiftConstants;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.impl.ProjectImpl;

/**
 * Utility class for access to OpenShiftUtil. It provides basic OpenShift client initialization and basic project handling.
 */
public class OpenShiftController {

    private static final String OPENSHIFT_URL = OpenShiftConstants.getOpenShiftUrl();
    private static final String OPENSHIFT_USERNAME = OpenShiftConstants.getOpenShiftUserName();
    private static final String OPENSHIFT_PASSWORD = OpenShiftConstants.getOpenShiftPassword();

    static {
        System.setProperty("xtf.config.master.url", OPENSHIFT_URL);
        System.setProperty("xtf.config.master.username", OPENSHIFT_USERNAME);
        System.setProperty("xtf.config.master.password", OPENSHIFT_PASSWORD);
        System.setProperty("xtf.config.master.admin.username", OPENSHIFT_USERNAME);
        System.setProperty("xtf.config.master.admin.password", OPENSHIFT_PASSWORD);
    }

    /**
     * @return OpenShiftUtil with default namespace configured.
     */
    public static OpenShiftUtil getOpenShiftUtil() {
        return OpenShiftUtils.masterUtil();
    }

    /**
     * @param projectName Namespace to be set to OpenShiftUtil.
     * @return OpenShiftUtil with project namespace configured.
     */
    public static OpenShiftUtil getOpenShiftUtil(String projectName) {
        return OpenShiftUtils.masterUtil(projectName);
    }

    /**
     * @param projectName Project name.
     * @return Project object representing created project.
     */
    public static Project createProject(String projectName) {
        try (OpenShiftUtil util = getOpenShiftUtil()) {
            util.createProjectRequest(projectName);

            return new ProjectImpl(projectName);
        }
    }

    /**
     * @param projectName Project name.
     */
    public static void deleteProject(String projectName) {
        try (OpenShiftUtil util = getOpenShiftUtil()) {
            util.deleteProject(projectName);
        }
    }
}

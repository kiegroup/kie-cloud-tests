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

import cz.xtf.core.config.OpenShiftConfig;
import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;
import org.kie.cloud.api.constants.ConfigurationInitializer;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.resource.impl.ProjectImpl;

/**
 * Utility class for access to OpenShift. It provides basic OpenShift client initialization and basic project handling.
 */
public class OpenShiftController {

    static {
        ConfigurationInitializer.initConfigProperties();
    }

    /**
     * @return OpenShift with default namespace configured.
     */
    public static OpenShift getOpenShift() {
        return getOpenShift(OpenShiftConfig.namespace());
    }

    /**
     * @param projectName Namespace to be set to OpenShift.
     * @return OpenShift with project namespace configured.
     */
    public static OpenShift getOpenShift(String projectName) {
        return OpenShifts.master(projectName);
    }

    /**
     * @return OpenShift admin with project namespace configured.
     */
    public static OpenShift getOpenShiftAdmin() {
        return getOpenShiftAdmin(OpenShiftConfig.namespace());
    }

    /**
     * @param projectName Namespace to be set to OpenShift.
     * @return OpenShift admin with project namespace configured.
     */
    public static OpenShift getOpenShiftAdmin(String projectName) {
        return OpenShifts.admin(projectName);
    }

    /**
     * @param projectName Project name.
     * @return Project object representing created project.
     */
    public static Project createProject(String projectName) {
        try (OpenShift openShift = getOpenShift()) {
            openShift.createProjectRequest(projectName);

            return new ProjectImpl(projectName);
        }
    }

    /**
     * @param projectName Project name.
     */
    public static void deleteProject(String projectName) {
        try (OpenShift openShift = getOpenShift()) {
            openShift.deleteProject(projectName);
        }
    }
}

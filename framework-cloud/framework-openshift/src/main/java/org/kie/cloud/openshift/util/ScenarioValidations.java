/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.openshift.util;

import org.kie.cloud.openshift.template.ProjectProfile;

/**
 * Utils to validate scenarios.
 */
public final class ScenarioValidations {

    private ScenarioValidations() {

    }

    /**
     * Verify that the template project for the execution is set to "jbpm". If not, it will raise:
     * - An UnsupportedOperationException exception if it sets to "drools".
     * - Otherwise, an IllegalStateException exception.
     */
    public static void verifyJbpmScenarioOnly() {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        switch (projectProfile) {
            case JBPM:
                return;
            case DROOLS:
                throw new UnsupportedOperationException("Not supported");
            default:
                throw new IllegalStateException("Unrecognized ProjectProfile: " + projectProfile);
        }
    }

    /**
     * Verify that the template project for the execution is set to "drools". If not, it will raise:
     * - An UnsupportedOperationException exception if it sets to "jbpm".
     * - Otherwise, an IllegalStateException exception.
     */
    public static void verifyDroolsScenarioOnly() {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        switch (projectProfile) {
            case JBPM:
                throw new UnsupportedOperationException("Not supported");
            case DROOLS:
                return;
            default:
                throw new IllegalStateException("Unrecognized ProjectProfile: " + projectProfile);
        }
    }
}

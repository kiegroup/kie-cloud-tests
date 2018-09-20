/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.template;

import java.util.Arrays;

public enum ProjectProfile {
    DROOLS("decision-central"),
    JBPM("business-central");

    private final String workbenchName;
    private static final String SYSTEM_PROPERTY_NAME = "template.project";

    ProjectProfile(String workbenchName) {
        this.workbenchName = workbenchName;
    }

    public String getWorkbenchName() {
        return workbenchName;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static ProjectProfile fromSystemProperty() {
        final String value = System.getProperty(SYSTEM_PROPERTY_NAME);
        return Arrays.stream(ProjectProfile.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Invalid value of system property %s='%s' (must be one of %s)"
                                , SYSTEM_PROPERTY_NAME, value, Arrays.toString(ProjectProfile.values())))
                );
    }
}

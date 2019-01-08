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

package org.kie.cloud.openshift.constants;

import org.kie.cloud.openshift.template.ProjectProfile;

/**
 * Property names that differ based on whether droos / jbpm variant of project is used.
 * <p>
 * Usage:
 * <pre>
 *     ProjectSpecificPropertyNames propertyNames = ProjectSpecificPropertyNames.create();
 *     envVariables.put(propertyNames.workbenchMavenUserName(), "myuser");
 * </pre>
 */
public interface ProjectApbSpecificPropertyNames {

    String workbenchMavenUserName();

    String workbenchMavenPassword();

    String workbenchHttpsSecret();

    String workbenchMavenService();

    String workbenchHostnameHttp();

    String workbenchHostnameHttps();

    String workbenchSsoClient();

    String workbenchSsoSecret();

    /**
     * @return create ProjectSpecificPropertyNames instance grouping names of properties
     * based on value of system property "template.project"
     */
    static ProjectApbSpecificPropertyNames create() {
        ProjectProfile projectProfile = ProjectProfile.fromSystemProperty();
        switch (projectProfile) {
            case JBPM:
                return new JbpmApbSpecificPropertyNames();
            case DROOLS:
                return new DroolsApbSpecificPropertyNames();
            default:
                throw new IllegalStateException("Unrecognized ProjectProfile: " + projectProfile);
        }
    }
}

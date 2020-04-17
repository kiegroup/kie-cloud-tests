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

package org.kie.cloud.tests.common.client.util;

/**
 * By default, projectname will be artifactname
 */
public enum Kjar {
    DEFINITION("org.kie.server.testing", "definition-project", "1.0.0.Final"),
    DEFINITION_SNAPSHOT("org.kie.server.testing", "definition-project-snapshot", "1.0.0-SNAPSHOT"),
    DEFINITION_101_SNAPSHOT("org.kie.server.testing", "definition-project-snapshot", "1.0.1-SNAPSHOT", "definition-project-101-snapshot"),
    CLOUD_BALANCE_SNAPSHOT("org.kie.server.testing", "cloudbalance-snapshot", "1.0.0-SNAPSHOT"),
    TIMER("org.kie.server.testing", "timer-project", "1.0.0-SNAPSHOT"),
    RULE_SNAPSHOT("org.kie.server.testing", "rule-project", "1.0.0-SNAPSHOT"),
    HELLO_RULES("org.kie.server.testing", "hello-rules", "1.0.0.Final"),
    HELLO_RULES_SNAPSHOT("org.kie.server.testing", "hello-rules-snapshot", "1.0.0-SNAPSHOT"),
    MIGRATION_PROJECT_100_SNAPSHOT("org.kie.server.testing", "migration-project", "1.0.0-SNAPSHOT", "migration-project-100-snapshot"),
    MIGRATION_PROJECT_200_SNAPSHOT("org.kie.server.testing", "migration-project", "2.0.0-SNAPSHOT", "migration-project-200-snapshot"),
    USERTASK("org.kie.server.testing", "usertask-project", "1.0.0.Final"),
    STATELESS_SESSION("org.kie.server.testing", "stateless-session", "1.0.0.Final"),
    EXTERNAL_SIGNAL("org.kie.server.testing", "external-signal", "1.0.0.Final");

    private String groupId;
    private String artifactName;
    private String version;
    private String projectName;

    private Kjar(String groupId, String artifactName, String version) {
        this(groupId, artifactName, version, artifactName);
    }

    private Kjar(String groupId, String artifactName, String version, String projectName) {
        this.groupId = groupId;
        this.artifactName = artifactName;
        this.version = version;
        this.projectName = projectName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getVersion() {
        return version;
    }

    public String getProjectName() {
        return projectName;
    }

    public String toString() {
        return groupId + ":" + artifactName + ":" + version;
    }
}

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

package org.kie.cloud.integrationtests;

public enum Kjar {
    DEFINITION("org.kie.server.testing", "definition-project", "1.0.0.Final"),
    DEFINITION_SNAPSHOT("org.kie.server.testing", "definition-project-snapshot", "1.0.0-SNAPSHOT"),
    DEFINITION_101_SNAPSHOT("org.kie.server.testing", "definition-project-snapshot", "1.0.1-SNAPSHOT"),
    CLOUD_BALANCE_SNAPSHOT("org.kie.server.testing", "cloudbalance-snapshot", "1.0.0-SNAPSHOT"),
    TIMER("org.kie.server.testing", "timer-project", "1.0.0.Final"),
    RULE_SNAPSHOT("org.kie.server.testing", "rule-project", "1.0.0-SNAPSHOT");

    private String groupId;
    private String name;
    private String version;

    private Kjar(String groupId, String name, String version) {
        this.groupId = groupId;
        this.name = name;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String toString() {
        return groupId + ":" + name + ":" + version;
    }
}

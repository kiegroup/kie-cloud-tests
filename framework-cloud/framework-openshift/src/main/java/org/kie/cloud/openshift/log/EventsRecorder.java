/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.log;

import io.fabric8.kubernetes.api.model.Event;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.resource.Project;

/**
 * Copy of https://github.com/xtf-cz/xtf/blob/xtf_0.12/junit5/src/main/java/cz/xtf/junit5/listeners/EventsRecorder.java.
 * The class cannot be used directly in this module as it depends on JUnit 5, this module is using JUnit 4.
 */
public class EventsRecorder {

    public static void recordProjectEvents(Project project, String logFolderName) {
        if (project == null) {
            return;
        }

        StringBuffer writer = new StringBuffer();
        writer.append("LAST SEEN");
        writer.append('\t');
        writer.append("FIRST SEEN");
        writer.append('\t');
        writer.append("COUNT");
        writer.append('\t');
        writer.append("NAME");
        writer.append('\t');
        writer.append("KIND");
        writer.append('\t');
        writer.append("SUBOBJECT");
        writer.append('\t');
        writer.append("TYPE");
        writer.append('\t');
        writer.append("REASON");
        writer.append('\t');
        writer.append("SOURCE");
        writer.append('\t');
        writer.append("MESSAGE");
        writer.append('\n');

        for (Event event : project.getOpenShift().getEvents()) {
            writer.append(event.getLastTimestamp());
            writer.append('\t');
            writer.append(event.getFirstTimestamp());
            writer.append('\t');
            writer.append("" + event.getCount());
            writer.append('\t');
            writer.append(event.getMetadata().getName());
            writer.append('\t');
            writer.append(event.getKind());
            writer.append('\t');
            writer.append(event.getInvolvedObject().getFieldPath());
            writer.append('\t');
            writer.append(event.getType());
            writer.append('\t');
            writer.append(event.getReason());
            writer.append('\t');
            writer.append(event.getSource().getComponent());
            writer.append('\t');
            writer.append(event.getMessage());
            writer.append('\n');
        }

        InstanceLogUtil.writeInstanceLogs(project.getName() + "-events", logFolderName, writer.toString());
    }
}

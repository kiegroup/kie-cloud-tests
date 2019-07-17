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

package org.kie.cloud.common.logs;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.kie.cloud.api.deployment.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(InstanceLogUtil.class);

    private static final String INSTANCES_LOGS_OUTPUT_DIRECTORY = "instance.logs";
    private static final String DEFAULT_LOG_OUTPUT_DIRECTORY = "instances";
    private static final String LOG_SUFFIX = ".log";

    public static void writeInstanceLogs(Instance instance, String customLogFolderName) {
        writeInstanceLogs(instance.getName(), customLogFolderName, instance.getLogs());
    }

    public static void writeInstanceLogs(String name, String customLogFolderName, String logs) {
        File logFile = getOutputFile(name, customLogFolderName);
        try {
            FileUtils.write(logFile, logs, "UTF-8");
        } catch (Exception e) {
            logger.error("Error writting instance logs", e);
        }
    }

    public static void appendInstanceLogLines(String instanceName, String customLogFolderName, Collection<String> lines) {
        File logFile = getOutputFile(instanceName, customLogFolderName);
        try {
            FileUtils.writeLines(logFile, "UTF-8", lines, true);
        } catch (Exception e) {
            logger.error("Error writting instance logs", e);
        }
    }

    private static File getOutputFile(String instanceName, String customLogFolderName) {
        File outputDirectory = new File(System.getProperty(INSTANCES_LOGS_OUTPUT_DIRECTORY, DEFAULT_LOG_OUTPUT_DIRECTORY), customLogFolderName);
        outputDirectory.mkdirs();
        return new File(outputDirectory, instanceName + LOG_SUFFIX);
    }
}

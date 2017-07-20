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
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.kie.cloud.api.deployment.Deployment;
import org.kie.cloud.api.deployment.Instance;

public class InstanceLogUtil {

    private static final String INSTANCES_LOGS_OUTPUT_DIRECTORY = "instance.logs";
    private static final String DEFAULT_LOG_OUTPUT_DIRECTORY = "instances";
    private static final String LOG_SUFFIX = ".log";

    public static void writeInstanceLogs(Instance instance) {
        File outputDirectory = new File(System.getProperty(INSTANCES_LOGS_OUTPUT_DIRECTORY, DEFAULT_LOG_OUTPUT_DIRECTORY));
        if (!outputDirectory.isDirectory()) {
            outputDirectory.mkdir();
        }

        File logFile = new File(outputDirectory, instance.getName() + LOG_SUFFIX);
        try {
            FileUtils.write(logFile, instance.getLogs(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Error writting instance logs", e);
        }
    }

    public static void writeDeploymentLogs(Deployment deployment) {
        List<Instance> instances = deployment.getInstances();
        for (Instance instance : instances) {
            writeInstanceLogs(instance);
        }
    }
}

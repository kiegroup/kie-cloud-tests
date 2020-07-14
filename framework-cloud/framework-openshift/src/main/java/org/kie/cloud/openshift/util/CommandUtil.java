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

package org.kie.cloud.openshift.util;

import java.io.ByteArrayOutputStream;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.internal.ExecWebSocketListener;
import org.kie.cloud.api.deployment.CommandExecutionResult;

public class CommandUtil {
    public static CommandExecutionResult runCommandImpl(PodResource<Pod, DoneablePod> pod, String... command) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        try (ExecWatch execWatch = pod.writingOutput(output).writingError(error).exec(command)) {

            waitUntilCommandIsFinished(execWatch);

            CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
            commandExecutionResult.setOutput(output.toString());
            commandExecutionResult.setError(error.toString());
            return commandExecutionResult;
        }

    }

    private static void waitUntilCommandIsFinished(ExecWatch execWatch) {
        if (execWatch instanceof ExecWebSocketListener) {
            ((ExecWebSocketListener) execWatch).waitUntilReady();
        }
    }
}

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

package org.kie.cloud.openshift.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for executing various bash commands.
 */
public class ProcessExecutor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * Execute command and wait until command is finished. Output and error streams are redirected to the logger.
     *
     * @param command Command to be executed.
     */
    public void executeProcessCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            Future<?> osFuture = executorService.submit(new ProcessOutputReader(process.getInputStream()));
            Future<?> esFuture = executorService.submit(new ProcessOutputReader(process.getErrorStream()));

            process.waitFor();
            osFuture.get();
            esFuture.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error executing command " + command, e);
        }
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private final class ProcessOutputReader implements Runnable {
        private InputStream fromStream;

        private ProcessOutputReader(InputStream fromStream) {
            this.fromStream = fromStream;
        }

        @Override
        public void run() {
            BufferedReader br = null;
            try {
                InputStreamReader isr = new InputStreamReader(fromStream);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    logger.info(line);
                }

            } catch (IOException ioe) {
                throw new RuntimeException("Error reading from stream!", ioe);
            } finally {
                try {
                    br.close();
                } catch (Exception ex) {
                    throw new RuntimeException("Error closing streams!", ex);
                }
            }
        }
    }
}

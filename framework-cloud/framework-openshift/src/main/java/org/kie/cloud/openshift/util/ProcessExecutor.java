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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

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
        executeProcessCommand(command, null);
    }

    /**
     * Execute command and wait until command is finished. Output and error streams are redirected to the logger.
     *
     * @param command Command to be executed.
     * @param directory Directory where the command should be executed.
     */
    public void executeProcessCommand(String command, Path directory) {
        Consumer<String> outputConsumer = s -> logger.info(s);
        executeProcessCommand(command, outputConsumer, directory);
    }

    /**
     * Execute command and wait until command is finished. Output and error streams are redirected to the temporary file.
     *
     * @param command Command to be executed.
     * @return Temp file containing process output.
     */
    public File executeProcessCommandToTempFile(String command) {
        StringBuffer sb = new StringBuffer();
        Consumer<String> outputConsumer = s -> sb.append(s).append("\n");

        boolean processedSuccessfully = executeProcessCommand(command, outputConsumer, null);

        if (!processedSuccessfully) {
            throw new RuntimeException("Error while processing command \"" + command + "\". Process output:\n" + sb.toString());
        }

        return saveStringToTempFile(sb.toString());
    }

    private File saveStringToTempFile(String fileContent) {
        try {
            File tempFile = File.createTempFile("openshift", ".yaml");
            try (PrintWriter out = new PrintWriter(tempFile)) {
                out.print(fileContent);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error while creating temp file.", e);
        }
    }

    /**
     * Execute command and wait until command is finished.
     *
     * @param command Command to be executed.
     * @param outputConsumer Consumer processing the process output.
     * @param directory Directory where the command should be executed.
     * @return True if process terminated normally, false in case of error during processing.
     */
    private boolean executeProcessCommand(String command, Consumer<String> outputConsumer, Path directory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            if (directory != null) {
                processBuilder = processBuilder.directory(directory.toFile());
            }
            Process process = processBuilder.start();

            Future<?> osFuture = executorService.submit(new ProcessOutputReader(process.getInputStream(), outputConsumer));
            Future<?> esFuture = executorService.submit(new ProcessOutputReader(process.getErrorStream(), outputConsumer));

            int exitValue = process.waitFor();
            osFuture.get();
            esFuture.get();
            return exitValue == 0;
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
        Consumer<String> outputConsumer;

        private ProcessOutputReader(InputStream fromStream, Consumer<String> outputConsumer) {
            this.fromStream = fromStream;
            this.outputConsumer = outputConsumer;
        }

        @Override
        public void run() {
            BufferedReader br = null;
            try {
                InputStreamReader isr = new InputStreamReader(fromStream);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    outputConsumer.accept(line);
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

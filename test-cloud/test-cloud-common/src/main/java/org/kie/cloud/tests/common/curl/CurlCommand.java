/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.cloud.tests.common.curl;

import org.kie.cloud.api.deployment.CommandExecutionResult;
import org.kie.cloud.api.deployment.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CurlCommand {

    private static final String GET_METHOD = "GET";

    private static final Logger LOGGER = LoggerFactory.getLogger(CurlCommand.class);

    private final Instance pod;

    private String host = "localhost";
    private int port = 8080;
    private String username;
    private String password;

    private CurlCommand(Instance pod) {
        this.pod = pod;
    }

    /**
     * Set the host parameter.
     * @param host
     * @return
     */
    public CurlCommand withHost(String host) {
        this.host = host;

        return this;
    }

    /**
     * Set the port parameter.
     * @param port
     * @return
     */
    public CurlCommand withPort(int port) {
        this.port = port;

        return this;
    }

    /**
     * Set the username for authentication.
     * @param username
     * @return
     */
    public CurlCommand withUsername(String username) {
        this.username = username;

        return this;
    }

    /**
     * Set the password for authentication.
     * @param password
     * @return
     */
    public CurlCommand withPassword(String password) {
        this.password = password;

        return this;
    }

    /**
     * Run the GET command over a concrete path.
     * @param path
     * @return
     */
    public String get(String path) {
        return runCommand(GET_METHOD, path);
    }

    private String runCommand(String method, String path) {
        String command = String.format("curl -u %s:%s -X %s http://%s:%s/%s", username, password, method, host, port, path);
        LOGGER.debug("Running command in {}: {}", pod.getName(), command);
        CommandExecutionResult result = pod.runCommand(command.split(" "));
        LOGGER.trace("Result in {}. Output: {}, Error: {}", pod.getName(), result.getOutput(), result.getError());
        return result.getOutput();
    }

    /**
     * Prepare the CURL command on the specified pod instance.
     * @param pod instance
     * @return CURL builder
     */
    public static final CurlCommand onInstance(Instance pod) {
        return new CurlCommand(pod);
    }
}

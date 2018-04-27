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

package org.kie.cloud.api.deployment;

import java.time.Instant;

/**
 * Running instance of deployment representation in cloud environment. One
 * instance of deployed application. Application is configured in
 *
 * @see Deployment
 */
public interface Instance {

    /**
     * Return cloud instance name.
     *
     * @return instance name
     */
    String getName();

    /**
     * Return instance namespace. Namespace is same for instance and Deployment.
     *
     * @see Deployment#getNamespace()
     *
     * @return Instance namespace
     */
    String getNamespace();

    CommandExecutionResult runCommand(String... command);

    /**
     * Return true if instance is currently running.
     *
     * @return True if instance is currently running.
     */
    boolean isRunning();

    /**
     * Return started time of currently running instance.
     *
     * @return Started time.
     * @throws IllegalStateException In case instance is not running currently.
     */
    Instant startedAt() throws IllegalStateException;

    /**
     * Return cloud instance logs.
     *
     * @return instance logs
     */
    String getLogs();
}

/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.operator.model.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * KieApp objects which can be deployed or specifically configured.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Objects {

    private Console console;
    private List<Server> servers = new ArrayList<>();
    private SmartRouter smartRouter;
    private ProcessMigration processMigration;

    public Console getConsole() {
        return console;
    }

    public ProcessMigration getProcessMigration() {
        return processMigration;
    }

    public void setProcessMigration(ProcessMigration processMigration) {
        this.processMigration = processMigration;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public void addServer(Server server) {
        this.servers.add(server);
    }

    public Server[] getServers() {
        return servers.toArray(new Server[0]);
    }

    public void setServers(Server[] servers) {
        this.servers = Arrays.asList(servers);
    }

    public SmartRouter getSmartRouter() {
        return smartRouter;
    }

    public void setSmartRouter(SmartRouter smartRouter) {
        this.smartRouter = smartRouter;
    }
}

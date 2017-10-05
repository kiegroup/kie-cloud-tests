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

package org.kie.cloud.api.deployment.constants;

import org.kie.cloud.api.constants.Constants;
import org.kie.cloud.api.constants.TestInfoPrinter;

public class DeploymentConstants implements Constants {

    static {
        TestInfoPrinter.printTestConstants();
    }

    public static final String KIE_SERVER_USER = "org.kie.server.user";
    public static final String KIE_SERVER_PASSWORD = "org.kie.server.pwd";

    public static final String WORKBENCH_USER = "org.kie.workbench.user";
    public static final String WORKBENCH_PASSWORD = "org.kie.workbench.pwd";

    public static final String CONTROLLER_USER = "org.kie.server.controller.user";
    public static final String CONTROLLER_PASSWORD = "org.kie.server.controller.pwd";

    public static final String DATABASE_HOST = "db.hostname";
    public static final String DATABASE_NAME = "database.name";
    public static final String EXTERNAL_DATABASE_NAME = "db.name";
    public static final String DATABASE_USERNAME = "db.username";
    public static final String DATABASE_PASSWORD = "db.password";

    public static String getKieServerUser() {
        return System.getProperty(KIE_SERVER_USER);
    }

    public static String getKieServerPassword() {
        return System.getProperty(KIE_SERVER_PASSWORD);
    }

    public static String getWorkbenchUser() {
        return System.getProperty(WORKBENCH_USER);
    }

    public static String getWorkbenchPassword() {
        return System.getProperty(WORKBENCH_PASSWORD);
    }

    public static String getControllerUser() {
        return System.getProperty(CONTROLLER_USER);
    }

    public static String getControllerPassword() {
        return System.getProperty(CONTROLLER_PASSWORD);
    }

    public static String getDatabaseHost() {
        return System.getProperty(DATABASE_HOST);
    }

    public static String getDatabaseName() {
        return System.getProperty(DATABASE_NAME);
    }

    public static String getExternalDatabaseName() {
        return System.getProperty(EXTERNAL_DATABASE_NAME);
    }

    public static String getDatabaseUsername() {
        return System.getProperty(DATABASE_USERNAME);
    }

    public static String getDatabasePassword() {
        return System.getProperty(DATABASE_PASSWORD);
    }
}

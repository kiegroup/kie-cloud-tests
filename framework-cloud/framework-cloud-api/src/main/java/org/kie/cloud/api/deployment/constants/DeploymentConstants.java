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

    public static final String DATABASE_NAME = "database.name";

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

    public static String getDatabaseName() {
        return System.getProperty(DATABASE_NAME);
    }
}

/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.constants;

public class ApbConstants {

    public class Plans {
        public static final String TRIAL = "trial";
        public static final String AUTHORING = "authoring";
        public static final String IMMUTABLE_KIE = "immutable-kie";
        public static final String IMMUTABLE_MON = "immutable-mon";
        public static final String MANAGED = "managed";
    }

    public class DbType {
        public static final String H2 = "H2";
        public static final String POSTGRE = "PostgreSQL";
        public static final String MYSQL = "MySQL";
        public static final String EXTERNAL = "External";
    }

    public class DefaultUser {
        public static final String KIE_ADMIN = "adminUser";
        public static final String KIE_SERVER_USER = "executionUser";
        public static final String CONTROLLER_USER = "controllerUser";
        public static final String MAVEN_USER = "mavenUser";
        public static final String PASSWORD = "RedHat";
    }
}

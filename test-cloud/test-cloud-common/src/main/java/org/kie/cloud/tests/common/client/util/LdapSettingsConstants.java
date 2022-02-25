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
package org.kie.cloud.tests.common.client.util;

public class LdapSettingsConstants {

    public static final String BIND_DN = "cn=Manager,dc=example,dc=com";
    public static final String BIND_CREDENTIAL = "admin";
    public static final String BASE_CTX_DN = "ou=people,dc=example,dc=com";
    public static final String BASE_FILTER = "uid";
    public static final String SEARCH_SCOPE = "SUBTREE_SCOPE";
    public static final Long SEARCH_TIME_LIMIT = 10000L;
    public static final String ROLE_ATTRIBUTE_ID = "cn";
    public static final String ROLES_CTX_DN = "ou=roles,dc=example,dc=com";
    public static final String ROLE_FILTER = "(member={1})";
    public static final Long ROLE_RECURSION = 1L;
    public static final String DEFAULT_ROLE = "guest";
    public static final String KIE_SERVER_DEFAULT_ROLE = "kie-server";

    public static final String ROLE_MAPPING = "test-admin=admin;test-kie-server=kie-server;test-rest-all=rest-all";
}

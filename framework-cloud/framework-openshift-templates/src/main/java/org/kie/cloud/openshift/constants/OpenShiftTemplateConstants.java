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

package org.kie.cloud.openshift.constants;

public class OpenShiftTemplateConstants {

    // Used as a generic password for all passwords in the template (Workbench user, Kie server user, Controller user, Workbench maven user)
    public static final String DEFAULT_PASSWORD = "DEFAULT_PASSWORD";

    public static final String KIE_ADMIN_USER = "KIE_ADMIN_USER";
    public static final String KIE_ADMIN_PWD = "KIE_ADMIN_PWD";

    public static final String GIT_HOOKS_DIR = "GIT_HOOKS_DIR";

    public static final String KIE_SERVER_USER = "KIE_SERVER_USER";
    public static final String KIE_SERVER_PWD = "KIE_SERVER_PWD";
    public static final String KIE_SERVER_HOST = "KIE_SERVER_HOST";
    public static final String KIE_SERVER_PORT = "KIE_SERVER_PORT";
    public static final String KIE_SERVER_ID = "KIE_SERVER_ID";

    public static final String KIE_SERVER_PERSISTENCE_DIALECT = "KIE_SERVER_PERSISTENCE_DIALECT";
    public static final String KIE_SERVER_PERSISTENCE_DS = "KIE_SERVER_PERSISTENCE_DS";
    public static final String KIE_SERVER_PERSISTENCE_TM = "KIE_SERVER_PERSISTENCE_TM";

    public static final String KIE_SERVER_CONTROLLER_PROTOCOL = "KIE_SERVER_CONTROLLER_PROTOCOL";
    public static final String KIE_SERVER_CONTROLLER_HOST = "KIE_SERVER_CONTROLLER_HOST";
    public static final String KIE_SERVER_CONTROLLER_PORT = "KIE_SERVER_CONTROLLER_PORT";
    public static final String KIE_SERVER_CONTROLLER_USER = "KIE_SERVER_CONTROLLER_USER";
    public static final String KIE_SERVER_CONTROLLER_PWD = "KIE_SERVER_CONTROLLER_PWD";
    public static final String KIE_SERVER_CONTROLLER_SERVICE = "KIE_SERVER_CONTROLLER_SERVICE";

    public static final String KIE_SERVER_ROUTER_ID = "KIE_SERVER_ROUTER_ID";
    public static final String KIE_SERVER_ROUTER_NAME = "KIE_SERVER_ROUTER_NAME";
    public static final String KIE_SERVER_ROUTER_PROTOCOL = "KIE_SERVER_ROUTER_PROTOCOL";
    public static final String KIE_SERVER_ROUTER_HOST = "KIE_SERVER_ROUTER_HOST";
    public static final String KIE_SERVER_ROUTER_PORT = "KIE_SERVER_ROUTER_PORT";
    public static final String KIE_SERVER_ROUTER_SERVICE = "KIE_SERVER_ROUTER_SERVICE";
    public static final String KIE_SERVER_ROUTER_URL_EXTERNAL = "KIE_SERVER_ROUTER_URL_EXTERNAL";

    public static final String KIE_SERVER_BYPASS_AUTH_USER = "KIE_SERVER_BYPASS_AUTH_USER";

    // Intentionally not public. Use ProjectSpecificPropertyNames to get proper variant based on product profile
    static final String BUSINESS_CENTRAL_MAVEN_SERVICE = "BUSINESS_CENTRAL_MAVEN_SERVICE";
    static final String BUSINESS_CENTRAL_MAVEN_USERNAME = "BUSINESS_CENTRAL_MAVEN_USERNAME";
    static final String BUSINESS_CENTRAL_MAVEN_PASSWORD = "BUSINESS_CENTRAL_MAVEN_PASSWORD";
    static final String DECISION_CENTRAL_MAVEN_SERVICE = "DECISION_CENTRAL_MAVEN_SERVICE";
    static final String DECISION_CENTRAL_MAVEN_USERNAME = "DECISION_CENTRAL_MAVEN_USERNAME";
    static final String DECISION_CENTRAL_MAVEN_PASSWORD = "DECISION_CENTRAL_MAVEN_PASSWORD";

    public static final String MAVEN_REPO_URL = "MAVEN_REPO_URL";
    public static final String MAVEN_REPO_USERNAME = "MAVEN_REPO_USERNAME";
    public static final String MAVEN_REPO_PASSWORD = "MAVEN_REPO_PASSWORD";

    public static final String IMAGE_STREAM_NAMESPACE = "IMAGE_STREAM_NAMESPACE";
    public static final String APPLICATION_NAME = "APPLICATION_NAME";
    public static final String KIE_SERVER_IMAGE_STREAM_NAME = "KIE_SERVER_IMAGE_STREAM_NAME";
    public static final String IMAGE_STREAM_TAG = "IMAGE_STREAM_TAG";

    public static final String KIE_SERVER_EXTERNALDB_HOST = "KIE_SERVER_EXTERNALDB_HOST";
    public static final String KIE_SERVER_EXTERNALDB_PORT = "KIE_SERVER_EXTERNALDB_PORT";
    public static final String KIE_SERVER_EXTERNALDB_URL = "KIE_SERVER_EXTERNALDB_URL";
    public static final String KIE_SERVER_EXTERNALDB_DRIVER = "KIE_SERVER_EXTERNALDB_DRIVER";
    public static final String KIE_SERVER_EXTERNALDB_DIALECT = "KIE_SERVER_EXTERNALDB_DIALECT";
    public static final String KIE_SERVER_EXTERNALDB_DB = "KIE_SERVER_EXTERNALDB_DB";
    public static final String KIE_SERVER_EXTERNALDB_USER = "KIE_SERVER_EXTERNALDB_USER";
    public static final String KIE_SERVER_EXTERNALDB_PWD = "KIE_SERVER_EXTERNALDB_PWD";

    public static final String EXTENSIONS_IMAGE_NAMESPACE = "EXTENSIONS_IMAGE_NAMESPACE";
    public static final String EXTENSIONS_IMAGE = "EXTENSIONS_IMAGE";

    // Intentionally not public. Use ProjectSpecificPropertyNames to get proper variant based on product profile
    static final String BUSINESS_CENTRAL_HOSTNAME_HTTP = "BUSINESS_CENTRAL_HOSTNAME_HTTP";
    static final String BUSINESS_CENTRAL_HOSTNAME_HTTPS = "BUSINESS_CENTRAL_HOSTNAME_HTTPS";
    static final String DECISION_CENTRAL_HOSTNAME_HTTP = "DECISION_CENTRAL_HOSTNAME_HTTP";
    static final String DECISION_CENTRAL_HOSTNAME_HTTPS = "DECISION_CENTRAL_HOSTNAME_HTTPS";
    public static final String CONTROLLER_HOSTNAME_HTTP = "CONTROLLER_HOSTNAME_HTTP";
    public static final String KIE_SERVER_HOSTNAME_HTTP = "KIE_SERVER_HOSTNAME_HTTP";
    public static final String KIE_SERVER_HOSTNAME_HTTPS = "KIE_SERVER_HOSTNAME_HTTPS";
    public static final String SMART_ROUTER_HOSTNAME_HTTP = "SMART_ROUTER_HOSTNAME_HTTP";
    public static final String KIE_SERVER1_HOSTNAME_HTTP = "KIE_SERVER1_HOSTNAME_HTTP";
    public static final String KIE_SERVER1_HOSTNAME_HTTPS = "KIE_SERVER1_HOSTNAME_HTTPS";
    public static final String KIE_SERVER2_HOSTNAME_HTTP = "KIE_SERVER2_HOSTNAME_HTTP";
    public static final String KIE_SERVER2_HOSTNAME_HTTPS = "KIE_SERVER2_HOSTNAME_HTTPS";

    public static final String KIE_SERVER_CONTAINER_DEPLOYMENT = "KIE_SERVER_CONTAINER_DEPLOYMENT";

    public static final String KIE_SERVER_SYNC_DEPLOY = "KIE_SERVER_SYNC_DEPLOY";

    public static final String DROOLS_SERVER_FILTER_CLASSES = "DROOLS_SERVER_FILTER_CLASSES";

    public static final String SOURCE_REPOSITORY_URL = "SOURCE_REPOSITORY_URL";
    public static final String SOURCE_REPOSITORY_REF = "SOURCE_REPOSITORY_REF";
    public static final String CONTEXT_DIR = "CONTEXT_DIR";
    public static final String ARTIFACT_DIR = "ARTIFACT_DIR";

    // Intentionally not public. Use ProjectSpecificPropertyNames to get proper variant based on product profile
    static final String BUSINESS_CENTRAL_HTTPS_SECRET = "BUSINESS_CENTRAL_HTTPS_SECRET";
    static final String DECISION_CENTRAL_HTTPS_SECRET = "DECISION_CENTRAL_HTTPS_SECRET";
    public static final String KIE_SERVER_HTTPS_SECRET = "KIE_SERVER_HTTPS_SECRET";

    public static final String TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL = "TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL";

    public static final String SSO_ADMIN_USERNAME = "SSO_ADMIN_USERNAME";
    public static final String SSO_ADMIN_PASSWORD = "SSO_ADMIN_PASSWORD";
    public static final String SSO_REALM = "SSO_REALM";
    public static final String SSO_SERVICE_USERNAME = "SSO_SERVICE_USERNAME";
    public static final String SSO_SERVICE_PASSWORD = "SSO_SERVICE_PASSWORD";

    public static final String SSO_URL = "SSO_URL";
    public static final String SSO_CLIENT = "SSO_CLIENT";
    public static final String SSO_SECRET = "SSO_SECRET";
    public static final String SSO_USERNAME = "SSO_USERNAME";
    public static final String SSO_PASSWORD = "SSO_PASSWORD";

    // Intentionally not public. Use ProjectSpecificPropertyNames to get proper variant based on product profile
    static final String BUSINESS_CENTRAL_SSO_CLIENT = "BUSINESS_CENTRAL_SSO_CLIENT";
    static final String BUSINESS_CENTRAL_SSO_SECRET = "BUSINESS_CENTRAL_SSO_SECRET";
    static final String DECISION_CENTRAL_SSO_CLIENT = "DECISION_CENTRAL_SSO_CLIENT";
    static final String DECISION_CENTRAL_SSO_SECRET = "DECISION_CENTRAL_SSO_SECRET";
    public static final String KIE_SERVER_SSO_CLIENT = "KIE_SERVER_SSO_CLIENT";
    public static final String KIE_SERVER_SSO_SECRET = "KIE_SERVER_SSO_SECRET";
    public static final String KIE_SERVER1_SSO_CLIENT = "KIE_SERVER1_SSO_CLIENT";
    public static final String KIE_SERVER1_SSO_SECRET = "KIE_SERVER1_SSO_SECRET";
    public static final String KIE_SERVER2_SSO_CLIENT = "KIE_SERVER2_SSO_CLIENT";
    public static final String KIE_SERVER2_SSO_SECRET = "KIE_SERVER2_SSO_SECRET";

    public static final String AUTH_LDAP_URL = "AUTH_LDAP_URL";
    public static final String AUTH_LDAP_BIND_DN = "AUTH_LDAP_BIND_DN";
    public static final String AUTH_LDAP_BIND_CREDENTIAL = "AUTH_LDAP_BIND_CREDENTIAL";
    public static final String AUTH_LDAP_JAAS_SECURITY_DOMAIN = "AUTH_LDAP_JAAS_SECURITY_DOMAIN";
    public static final String AUTH_LDAP_BASE_CTX_DN = "AUTH_LDAP_BASE_CTX_DN";
    public static final String AUTH_LDAP_BASE_FILTER = "AUTH_LDAP_BASE_FILTER";
    public static final String AUTH_LDAP_SEARCH_SCOPE = "AUTH_LDAP_SEARCH_SCOPE";
    public static final String AUTH_LDAP_SEARCH_TIME_LIMIT = "AUTH_LDAP_SEARCH_TIME_LIMIT";
    public static final String AUTH_LDAP_DISTINGUISHED_NAME_ATTRIBUTE = "AUTH_LDAP_DISTINGUISHED_NAME_ATTRIBUTE";
    public static final String AUTH_LDAP_PARSE_USERNAME = "AUTH_LDAP_PARSE_USERNAME";
    public static final String AUTH_LDAP_USERNAME_BEGIN_STRING = "AUTH_LDAP_USERNAME_BEGIN_STRING";
    public static final String AUTH_LDAP_USERNAME_END_STRING = "AUTH_LDAP_USERNAME_END_STRING";
    public static final String AUTH_LDAP_ROLE_ATTRIBUTE_ID = "AUTH_LDAP_ROLE_ATTRIBUTE_ID";
    public static final String AUTH_LDAP_ROLES_CTX_DN = "AUTH_LDAP_ROLES_CTX_DN";
    public static final String AUTH_LDAP_ROLE_FILTER = "AUTH_LDAP_ROLE_FILTER";
    public static final String AUTH_LDAP_ROLE_RECURSION = "AUTH_LDAP_ROLE_RECURSION";
    public static final String AUTH_LDAP_DEFAULT_ROLE = "AUTH_LDAP_DEFAULT_ROLE";
    public static final String AUTH_LDAP_ROLE_NAME_ATTRIBUTE_ID = "AUTH_LDAP_ROLE_NAME_ATTRIBUTE_ID";
    public static final String AUTH_LDAP_PARSE_ROLE_NAME_FROM_DN = "AUTH_LDAP_PARSE_ROLE_NAME_FROM_DN";
    public static final String AUTH_LDAP_ROLE_ATTRIBUTE_IS_DN = "AUTH_LDAP_ROLE_ATTRIBUTE_IS_DN";
    public static final String AUTH_LDAP_REFERRAL_USER_ATTRIBUTE_ID_TO_CHECK = "AUTH_LDAP_REFERRAL_USER_ATTRIBUTE_ID_TO_CHECK";

    public static final String MYSQL_IMAGE_STREAM_NAMESPACE = "MYSQL_IMAGE_STREAM_NAMESPACE";
    public static final String POSTGRESQL_IMAGE_STREAM_NAMESPACE = "POSTGRESQL_IMAGE_STREAM_NAMESPACE";

    public static final String AMQ_IMAGE_STREAM_NAMESPACE = "AMQ_IMAGE_STREAM_NAMESPACE";
    public static final String AMQ_IMAGE_STREAM_NAME = "AMQ_IMAGE_STREAM_NAME";
    public static final String AMQ_IMAGE_STREAM_TAG = "AMQ_IMAGE_STREAM_TAG";

    // CORS constants
    public static final String KIE_SERVER_ACCESS_CONTROL_ALLOW_ORIGIN = "KIE_SERVER_ACCESS_CONTROL_ALLOW_ORIGIN";
    public static final String KIE_SERVER_ACCESS_CONTROL_ALLOW_METHODS = "KIE_SERVER_ACCESS_CONTROL_ALLOW_METHODS";
    public static final String KIE_SERVER_ACCESS_CONTROL_ALLOW_HEADERS = "KIE_SERVER_ACCESS_CONTROL_ALLOW_HEADERS";
    public static final String KIE_SERVER_ACCESS_CONTROL_ALLOW_CREDENTIALS = "KIE_SERVER_ACCESS_CONTROL_ALLOW_CREDENTIALS";
    public static final String KIE_SERVER_ACCESS_CONTROL_MAX_AGE = "KIE_SERVER_ACCESS_CONTROL_MAX_AGE";
}

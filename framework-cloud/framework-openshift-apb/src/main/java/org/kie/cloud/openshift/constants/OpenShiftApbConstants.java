/*
 * Copyright 2018 JBoss by Red Hat.
 *
 * Licensed under the Apache License = "";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing = "";
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND = "";
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.openshift.constants;

public class OpenShiftApbConstants {

    //extra-vars for APB image
    public static final String APB_PLAN_ID = "_apb_plan_id";
    public static final String APB_KIESERVER_DB_TYPE = "apb_kieserver_db_type";

    // Pre-provisioned users
    public static final String KIE_ADMIN_USER = "apb_kie_admin_user";
    public static final String KIE_ADMIN_PWD = "apb_kie_admin_pwd";
    public static final String KIE_SERVER_USER = "apb_kieserver_user";
    public static final String KIE_SERVER_PWD = "apb_kieserver_pwd";
    public static final String KIE_CONTROLLER_USER = "apb_controller_user";
    public static final String KIE_CONTROLLER_PWD = "apb_controller_pwd";

    public static final String BUSINESS_CENTRAL_HOSTNAME_HTTPS = "apb_businesscentral_hostname";
    public static final String APB_KIESERVER_HOSTNAME_HTTPS = "apb_kieserver_hostname";
    public static final String APB_SMART_ROUTER_HOSTNAME_HTTPS = "apb_smartrouter_hostname";
    public static final String APB_KIESERVER_IMAGE_STREAM_NAME = "apb_kieserver_image_stream_name";
    public static final String APB_BUSINESSCENTRAL_SECRET_NAME = "apb_businesscentral_secret_name";
    public static final String APB_KIESERVER_SECRET_NAME = "apb_kieserver_secret_name";

    // RH-SSO
    public static final String SSO_URL = "apb_sso_url";
    public static final String SSO_REALM = "apb_sso_realm";
    public static final String BUSINESS_CENTRAL_SSO_CLIENT = "apb_sso_businesscentral_client";
    public static final String BUSINESS_CENTRAL_SSO_SECRET = "apb_sso_businesscentral_client_secret";
    public static final String KIE_SERVER_SSO_CLIENT = "apb_kieserver_sso_client";
    public static final String KIE_SERVER_SSO_SECRET = "apb_kieserver_sso_client_secret";
    //public static final String SSO_CLIENT = "apb_sso_client";
    //public static final String SSO_CLIENT_SECRET = "apb_sso_client_secret";
    public static final String SSO_USER = "apb_sso_user";
    public static final String SSO_PWD = "apb_sso_pwd";
    public static final String SSO_DISABLE_SSL_CERT_VALIDATION = "apb_sso_disable_ssl_cert_validation";
    public static final String SSO_PRINCIPAL_ATTRIBUTE = "apb_sso_principal_attribute";

    // Maven repositories
    public static final String MAVEN_REPO_URL = "apb_maven_repo_url";
    public static final String MAVEN_REPO_USER = "apb_maven_repo_user";
    public static final String MAVEN_REPO_PWD = "apb_maven_repo_pwd";
    public static final String BUSINESS_CENTRAL_MAVEN_USERNAME = "apb_businesscentral_maven_repo_user";
    public static final String BUSINESS_CENTRAL_MAVEN_PASSWORD = "apb_businesscentral_maven_repo_pwd";
    public static final String BUSINESS_CENTRAL_MAVEN_SERVICE = "";

    // ImageStreams
    public static final String IMAGE_STREAM_NAMESPACE = "image_stream_namespace"; // Need to hard replace this value
    public static final String APB_IMAGE_STREAM_TAG = "apb_image_stream_tag";

    public static final String DEFAULT_PASSWORD = "";

    public static final String KIE_SERVER_ID = "";
    public static final String KIE_SERVER_ROUTER_ID = "";
    public static final String TIMER_SERVICE_DATA_STORE_REFRESH_INTERVAL = ""; // ??

    // HA
    public static final String APB_REPLICAS = "apb_replicas";
    public static final String APB_KIESERVER_SETS = "apb_kieserver_sets";
    public static final String APB_KIESERVER_REPLICAS = "apb_kieserver_replicas";
    public static final String APB_SMARTROUTER_REPLICAS = "apb_smartrouter_replicas";
    public static final String APB_BUSINESSCENTRAL_REPLICAS = "apb_businesscentral_replicas";

    // Generic Keystore parameters
    //public static final String APB_SECRET_NAME = "apb_secret_name";
    //public static final String APB_KEYSTORE_ALIAS = "apb_keystore_alias";
    //public static final String APB_KEYSTORE_PWD = "apb_keystore_pwd";
    public static final String BUSINESSCENTRAL_SECRET_NAME = "apb_businesscentral_secret_name";
    public static final String BUSINESSCENTRAL_KEYSTORE_ALIAS = "apb_businesscentral_keystore_alias";
    public static final String BUSINESSCENTRAL_KEYSTORE_PWD = "apb_businesscentral_keystore_pwd";
    public static final String KIESERVER_SECRET_NAME = "apb_kieserver_secret_name";
    public static final String KIESERVER_KEYSTORE_ALIAS = "apb_kieserver_keystore_alias";
    public static final String KIESERVER_KEYSTORE_PWD = "apb_kieserver_keystore_pwd";

    // Artifact source (S2I)
    public static final String KIE_SERVER_CONTAINER_DEPLOYMENT = "apb_kieserver_container_deployment";
    public static final String SOURCE_REPOSITORY_URL = "apb_kieserver_source_url";
    public static final String SOURCE_REPOSITORY_REF = "apb_kieserver_source_ref";
    public static final String CONTEXT_DIR = "apb_kieserver_source_context";
    public static final String ARTIFACT_DIR = "apb_kieserver_artifact_dir";

    // Git hooks dir
    public static final String GIT_HOOKS_DIR = "apb_git_hooks_dir";

    // Router integration
    public static final String APB_ROUTER_SERVICE = "apb_router_svc";
    public static final String APB_ROUTER_HOST = "apb_router_host";
    public static final String APB_ROUTER_PORT = "apb_router_port";
    public static final String APB_ROUTER_PROTOCOL = "apb_router_protocol";

    // Controller integration
    public static final String APB_CONTROLLER_SERVICE = "apb_controller_svc";
    public static final String APB_CONTROLLER_HOST = "apb_controller_host";
    public static final String APB_CONTROLLER_PORT = "apb_controller_port";
    public static final String APB_CONTROLLER_PROTOCOL = "apb_controller_protocol";

    // Volume size (Required parameters)
    public static final String SMARTROUTER_VOLUME_SIZE = "apb_smartrouter_volume_size";
    public static final String BUSINESSCENTRAL_VOLUME_SIZE = "apb_businesscentral_volume_size";

    // LDAP
    public static final String AUTH_LDAP_URL = "apb_auth_ldap_url";
    public static final String AUTH_LDAP_BIND_DN = "apb_auth_ldap_bind_dn";
    public static final String AUTH_LDAP_BIND_CREDENTIAL = "apb_auth_ldap_bind_credential";
    public static final String AUTH_LDAP_JAAS_SECURITY_DOMAIN = "apb_auth_ldap_jaas_security_domain";
    public static final String AUTH_LDAP_BASE_CTX_DN = "apb_auth_ldap_base_ctx_dn";
    public static final String AUTH_LDAP_BASE_FILTER = "apb_auth_ldap_base_filter";
    public static final String AUTH_LDAP_SEARCH_SCOPE = "apb_auth_ldap_search_scope";
    public static final String AUTH_LDAP_SEARCH_TIME_LIMIT = "apb_auth_ldap_search_time_limit";
    public static final String AUTH_LDAP_DISTINGUISHED_NAME_ATTRIBUTE = "apb_auth_ldap_distinguished_name_attribute";
    public static final String AUTH_LDAP_PARSE_USERNAME = "apb_auth_ldap_parse_username";
    public static final String AUTH_LDAP_USERNAME_BEGIN_STRING = "apb_auth_ldap_username_begin_string";
    public static final String AUTH_LDAP_USERNAME_END_STRING = "apb_auth_ldap_username_end_string";
    public static final String AUTH_LDAP_ROLE_ATTRIBUTE_ID = "apb_auth_ldap_role_attribute_id";
    public static final String AUTH_LDAP_ROLES_CTX_DN = "apb_auth_ldap_roles_ctx_dn";
    public static final String AUTH_LDAP_ROLE_FILTER = "apb_auth_ldap_role_filter";
    public static final String AUTH_LDAP_ROLE_RECURSION = "apb_auth_ldap_role_recursion";
    public static final String AUTH_LDAP_DEFAULT_ROLE = "apb_auth_ldap_default_role";
    public static final String AUTH_LDAP_ROLE_NAME_ATTRIBUTE_ID = "apb_auth_ldap_role_name_attribute_id";
    public static final String AUTH_LDAP_PARSE_ROLE_NAME_FROM_DN = "apb_auth_ldap_parse_role_name_from_dn";
    public static final String AUTH_LDAP_ROLE_ATTRIBUTE_IS_DN = "apb_auth_ldap_role_attribute_is_dn";
    public static final String AUTH_LDAP_REFERRAL_USER_ATTRIBUTE_ID_TO_CHECK = "apb_auth_ldap_referral_user_attribute_id_to_check";

    // External Database
    public static final String KIE_SERVER_EXTERNALDB_DRIVER = "apb_kieserver_external_db_driver";
    public static final String KIE_SERVER_EXTERNALDB_HOST = "apb_kieserver_external_db_host";
    public static final String KIE_SERVER_EXTERNALDB_PORT = "apb_kieserver_external_db_port";
    public static final String KIE_SERVER_EXTERNALDB_DB = "apb_kieserver_external_db_name";
    public static final String KIE_SERVER_EXTERNALDB_USER = "";
    public static final String KIE_SERVER_EXTERNALDB_PWD = "";
    public static final String KIE_SERVER_EXTERNALDB_DIALECT = "apb_kieserver_external_db_dialect";
    public static final String KIE_SERVER_EXTERNALDB_URL = "apb_kieserver_external_db_url";

    public static final String KIE_SERVER_IMAGE_STREAM_NAME = "apb_kieserver_image_stream_name";
}

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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import cz.xtf.core.config.OpenShiftConfig;
import org.kie.cloud.api.constants.Constants;

public class OpenShiftConstants implements Constants {

    public static final String OPENSHIFT_URL = "openshift.master.url";
    public static final String OPENSHIFT_USER = "openshift.username";
    public static final String OPENSHIFT_PASSWORD = "openshift.password";
    public static final String OPENSHIFT_ADMIN_USER = "openshift.admin.username";
    public static final String OPENSHIFT_ADMIN_PASSWORD = "openshift.admin.password";
    public static final String OPENSHIFT_VERSION = "openshift.version";

    /**
     * Project name prefix - to simplify identification of projects created in OpenShift.
     */
    public static final String NAMESPACE_PREFIX = "openshift.namespace.prefix";
    /**
     * Property name to configure Openshift router timeout.
     */
    public static final String HAPROXY_ROUTER_TIMEOUT = "haproxy.router.openshift.io/timeout";
    /**
     * Property name to configure Openshift router balance.
     */
    public static final String HAPROXY_ROUTER_BALANCE = "haproxy.router.openshift.io/balance";
    /**
     * Used Kie application name. Needed for identification of services within the project.
     */
    public static final String KIE_APP_NAME = "kie.app.name";

    public static final String SECRET_NAME = "SECRET_NAME";
    /**
     * URL pointing to OpenShift resource file containing keystore for HTTPS communication.
     */
    public static final String KIE_APP_SECRET = "kie.app.secret";
    /**
     * URL pointing to OpenShift resource file containing keystore for HTTPS communication.
     */
    public static final String CUSTOM_TRUSTED_APP_SECRET = "custom.trusted.app.secret";

    /**
     * URL pointing to OpenShift resource file containing image streams with all available images.
     */
    public static final String KIE_IMAGE_STREAMS = "kie.image.streams";

    /**
     * URL pointing to OpenShift template file containing Workbench and Kie server.
     */
    public static final String KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER = "kie.app.template.workbench.kie-server";

    /**
     * URL pointing to OpenShift template file containing Workbench and Kie server.
     */
    public static final String KIE_APP_TEMPLATE_WORKBENCH_KIE_SERVER_PERSISTENT = "kie.app.template.workbench.kie-server.persistent";

    /**
     * URL pointing to OpenShift template file containing Kie server and external database.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_DATABASE_EXTERNAL = "kie.app.template.kie-server.database.external";

    /**
     * URL pointing to OpenShift template file containing just Kie server.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER = "kie.app.template.kie-server";
    /**
     * URL pointing to OpenShift template file containing just Kie server S2I.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_HTTPS_S2I = "kie.app.template.kie-server-https-s2i";
    /**
     * URL pointing to OpenShift template file containing just Kie server S2I with AMQ.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_S2I_AMQ = "kie.app.template.kie-server-s2i-amq";

    /**
     * URL pointing to OpenShift template file containing Kie server and PostgreSQL database.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_POSTGRESQL = "kie.app.template.kie-server.postgresql";

    /**
     * URL pointing to OpenShift template file containing Kie server and MySQL database.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_MYSQL = "kie.app.template.kie-server.mysql";

    /**
     * URL pointing to OpenShift template file containing Workbench monitoring console and Smart router.
     */
    public static final String KIE_APP_TEMPLATE_CONSOLE_SMARTROUTER = "kie.app.template.workbench-monitoring.smartrouter";

    /**
     * URL pointing to OpenShift template file containing clustered Workbench monitoring console Smart router, two Kie servers and two databases.
     */
    public static final String CLUSTERED_CONSOLE_SMARTROUTER_TWO_KIE_SERVERS_TWO_DATABASES = "kie.app.template.clustered-workbench-monitoring.smartrouter.two-kieservers.two-databases";

    /**
     * URL pointing to OpenShift template file containing clustered Workbench monitoring console, clustered Kie servers and a database.
     */
    public static final String CLUSTERED_CONSOLE_CLUSTERED_KIE_SERVER_DATABASE = "kie.app.template.clustered-workbench-monitoring.clustered-kieserver.database";

    /**
     * URL pointing to OpenShift template file containing clustered Workbench and Kie server.
     */
    public static final String CLUSTERED_WORKBENCH_KIE_SERVER_PERSISTENT = "kie.app.template.clustered-workbench.kieserver";

    /**
     * URL pointing to OpenShift template file containing clustered Workbench, Kie server and database.
     */
    public static final String CLUSTERED_WORKBENCH_KIE_SERVER_DATABASE_PERSISTENT = "kie.app.template.clustered-workbench.kieserver.database";

    /**
     * URL pointing to OpenShift template file containing Workbench monitoring console.
     */
    public static final String KIE_APP_TEMPLATE_CONSOLE = "kie.app.template.workbench-monitoring";

    /**
     * URL pointing to OpenShift template file containing Smart router.
     */
    public static final String KIE_APP_TEMPLATE_SMARTROUTER = "kie.app.template.smartrouter";

    /**
     * URL pointing to OpenShift template file containing Controller.
     */
    public static final String KIE_APP_TEMPLATE_CONTROLLER = "kie.app.template.controller";
    /**
     * URL pointing to OpenShift template file containing Workbench.
     */
    public static final String KIE_APP_TEMPLATE_WORKBENCH = "kie.app.template.workbench";

    public static final String KIE_APP_TEMPLATE_PROD_IMMUTABLE_MONITOR = "kie.app.template.prod.immutable.monitor";

    /**
     * A key pointing to a URL to OpenShift template for optaweb-employee-rostering app.
     */
    public static final String OPTAWEB_EMPLOYEE_ROSTERING_TEMPLATE = "kie.app.template.optaweb.employee-rostering";

    public static final String SSO_APP_TEMPLATE ="sso.app.template";
    public static final String SSO_APP_SECRETS = "sso.app.secrets";
    public static final String SSO_IMAGE_STREAMS = "sso.image.streams";

    public static final String APB_IMAGE_STREAM_NAME = "apb.image.stream.name";
    public static final String APB_IMAGE_DOCKER_REPO_TAG = "apb.image.docker.repo.tag";
    public static final String APB_KIE_IAMGE_STREAM_TAG = "apb.kie.image.stream.tag";

    public static final String KIE_IMAGE_TAG_AMQ = "kie.image.tag.amq";
    public static final String KIE_IMAGE_TAG_CONSOLE = "kie.image.tag.console";
    public static final String KIE_IMAGE_TAG_CONTROLLER = "kie.image.tag.controller";
    public static final String KIE_IMAGE_TAG_KIE_SERVER = "kie.image.tag.kieserver";
    public static final String KIE_IMAGE_TAG_MYSQL = "kie.image.tag.mysql";
    public static final String KIE_IMAGE_TAG_POSTGRESQL = "kie.image.tag.postgresql";
    public static final String KIE_IMAGE_TAG_SMARTROUTER = "kie.image.tag.smartrouter";
    public static final String KIE_IMAGE_TAG_WORKBENCH = "kie.image.tag.workbench";
    public static final String KIE_IMAGE_TAG_WORKBENCH_INDEXING = "kie.image.tag.workbench.indexing";

    /* HA CEP properties */
    public static final String AMQ_STREAMS_ZIP = "amq.streams.zip";
    public static final String AMQ_STREAMS_DIR = "amqStreamsDirectory";
    public static final String HA_CEP_RESOURCES_LIST = "ha.cep.resources";

    /**
     * File path pointing to folder containing JDBC driver scripts.
     */
    public static final String KIE_JDBC_DRIVER_SCRIPTS = "kie.jdbc.driver.scripts";

    /**
     * URL pointing to JDBC driver binary.
     */
    public static final String KIE_JDBC_DRIVER_BINARY_URL = "kie.jdbc.driver.binary.url";

    /**
     * URL pointing to running LDAP.
     */
    public static final String LDAP_URL = "ldap.url";

    public static String getOpenShiftUrl() {
        return System.getProperty(OPENSHIFT_URL);
    }

    public static String getOpenShiftUserName() {
        return System.getProperty(OPENSHIFT_USER);
    }

    public static String getOpenShiftPassword() {
        return System.getProperty(OPENSHIFT_PASSWORD);
    }

    public static String getOpenShiftAdminUserName() {
        return System.getProperty(OPENSHIFT_ADMIN_USER);
    }

    public static String getOpenShiftAdminPassword() {
        return System.getProperty(OPENSHIFT_ADMIN_PASSWORD);
    }

    public static String getOpenShiftVersion() {
        return System.getProperty(OPENSHIFT_VERSION);
    }

    public static Optional<String> getNamespacePrefix() {
        return Optional.ofNullable(System.getProperty(NAMESPACE_PREFIX));
    }

    public static String getKieAppSecret() {
        return System.getProperty(KIE_APP_SECRET);
    }

    public static String getCustomTrustedAppSecret() {
        return System.getProperty(CUSTOM_TRUSTED_APP_SECRET);
    }

    public static String getKieImageStreams() {
        return System.getProperty(KIE_IMAGE_STREAMS);
    }

    public static String getSsoImageStreams() {
        return System.getProperty(SSO_IMAGE_STREAMS);
    }

    public static String getApbImageStreamName() {
        return System.getProperty(APB_IMAGE_STREAM_NAME);
    }

    public static String getApbImageDockerRepoTag() {
        return System.getProperty(APB_IMAGE_DOCKER_REPO_TAG);
    }

    public static String getApbKieImageStreamTag() {
        return System.getProperty(APB_KIE_IAMGE_STREAM_TAG);
    }

    public static String getLdapUrl() {
        return System.getProperty(LDAP_URL);
    }

    public static String getKieApplicationName() {
        return System.getProperty(KIE_APP_NAME);
    }

    public static String getAMQStreamsZip() {
        return System.getProperty(AMQ_STREAMS_ZIP);
    }

    public static String getAMQStreamsDir() {
        return System.getProperty(AMQ_STREAMS_DIR);
    }

    public static String getHaCepResourcesList() {
        return System.getProperty(HA_CEP_RESOURCES_LIST);
    }

    public static File getKieJdbcDriverScriptsFolder() {
        String kieJdbcDriverScriptsFolder = System.getProperty(KIE_JDBC_DRIVER_SCRIPTS);
        return new File(kieJdbcDriverScriptsFolder);
    }

    public static URL getKieJdbcDriverBinaryUrl() {
        String kieJdbcDriverBinaryUrl = System.getProperty(KIE_JDBC_DRIVER_BINARY_URL);
        try {
            return new URL(kieJdbcDriverBinaryUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL of Kie server JDBC driver binary.", e);
        }
    }

    /**
     * @return Name of the secret containing keystore file for HTTPS communication.
     */
    public static String getKieApplicationSecretName() {
        return "kie-app-secret";
    }

    @Override
    public void initConfigProperties() {
        // init XTF configuration for OpenShift
        System.setProperty(OpenShiftConfig.OPENSHIFT_URL, getOpenShiftUrl());
        System.setProperty(OpenShiftConfig.OPENSHIFT_MASTER_USERNAME, getOpenShiftUserName());
        System.setProperty(OpenShiftConfig.OPENSHIFT_MASTER_PASSWORD, getOpenShiftPassword());
        System.setProperty(OpenShiftConfig.OPENSHIFT_ADMIN_USERNAME, getOpenShiftAdminUserName() != null ? getOpenShiftAdminUserName() : getOpenShiftUserName());
        System.setProperty(OpenShiftConfig.OPENSHIFT_ADMIN_PASSWORD, getOpenShiftAdminPassword() != null ? getOpenShiftAdminPassword() : getOpenShiftPassword());
        System.setProperty(OpenShiftConfig.OPENSHIFT_VERSION, getOpenShiftVersion());
        System.setProperty(OpenShiftConfig.OPENSHIFT_NAMESPACE, "openshift");
    }
}

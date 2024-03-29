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
import java.util.Optional;

import cz.xtf.core.config.OpenShiftConfig;
import org.kie.cloud.api.constants.Constants;

public class OpenShiftConstants implements Constants {

    public static final String OPENSHIFT_URL = "openshift.master.url";
    public static final String OPENSHIFT_USER = "openshift.username";
    public static final String OPENSHIFT_PASSWORD = "openshift.password";
    public static final String OPENSHIFT_TOKEN = "openshift.token";
    public static final String OPENSHIFT_ADMIN_USER = "openshift.admin.username";
    public static final String OPENSHIFT_ADMIN_PASSWORD = "openshift.admin.password";
    public static final String OPENSHIFT_ADMIN_TOKEN = "openshift.admin.token";
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

    public static final String KIE_ADMIN_USER = "KIE_ADMIN_USER";
    public static final String KIE_ADMIN_PWD = "KIE_ADMIN_PWD";

    public static final String CREDENTIALS_SECRET = "CREDENTIALS_SECRET";
    /**
     * File containing the SSL certificate for HTTPS commnication.
     */
    public static final String TRUSTED_KEYSTORE_FILE = "trusted.keystore.file";

    /**
     * URL pointing to OpenShift resource file containing image streams with all available images.
     */
    public static final String KIE_IMAGE_STREAMS = "kie.image.streams";

    /**
     * URL pointing to OpenShift resource file containing image stream with mirrored Nexus image (from docker hub).
     */
    public static final String NEXUS_MIRROR_IMAGE_STREAM = "nexus.mirror.image.stream";

    /**
     * URL pointing to OpenShift resource file containing image stream with mirrored Registry image (from docker hub).
     */
    public static final String REGISTRY_MIRROR_IMAGE_STREAM = "registry.mirror.image.stream";

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
     * URL pointing to OpenShift template file containing just Kie server S2I with database.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_DATABASE_HTTPS_S2I = "kie.app.template.kie-server-database-https-s2i";
    /**
     * URL pointing to OpenShift template file containing just Kie server S2I with AMQ.
     */
    public static final String KIE_APP_TEMPLATE_KIE_SERVER_DATABASE_S2I_AMQ = "kie.app.template.kie-server-database-s2i-amq";

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
     * A key pointing to a URL to OpenShift template for optaweb-employee-rostering app.
     */
    public static final String OPTAWEB_EMPLOYEE_ROSTERING_TEMPLATE = "kie.app.template.optaweb.employee-rostering";

    public static final String SSO_APP_TEMPLATE ="sso.app.template";
    public static final String SSO_APP_SECRETS = "sso.app.secrets";
    public static final String SSO_IMAGE_STREAMS = "sso.image.streams";

    public static final String PROMETHEUS_VERSION = "prometheus.version";

    public static final String AMQ_IMAGE_STREAMS = "amq.image.streams";

    public static final String KIE_IMAGE_TAG_AMQ = "kie.image.tag.amq";
    public static final String KIE_IMAGE_TAG_CONSOLE = "kie.image.tag.console";
    public static final String KIE_IMAGE_TAG_CONTROLLER = "kie.image.tag.controller";
    public static final String KIE_IMAGE_TAG_KIE_SERVER = "kie.image.tag.kieserver";
    public static final String KIE_IMAGE_TAG_MYSQL = "kie.image.tag.mysql";
    public static final String KIE_IMAGE_TAG_OPERATOR = "kie.image.tag.operator";
    public static final String KIE_IMAGE_TAG_POSTGRESQL = "kie.image.tag.postgresql";
    public static final String KIE_IMAGE_TAG_PROCESS_MIGRATION = "kie.image.tag.process.migration";
    public static final String KIE_IMAGE_TAG_SMARTROUTER = "kie.image.tag.smartrouter";
    public static final String KIE_IMAGE_TAG_WORKBENCH = "kie.image.tag.workbench";
    public static final String KIE_IMAGE_TAG_WORKBENCH_INDEXING = "kie.image.tag.workbench.indexing";

    /* HA CEP properties */
    public static final String AMQ_STREAMS_ZIP = "amq.streams.zip";
    public static final String AMQ_STREAMS_DIR = "amqStreamsDirectory";
    public static final String HA_CEP_SOURCES_DIR = "ha.cep.sources.dir";
    public static final String PROJECT_BUILD_DIRECTORY = "project.build.directory";

    /**
     * File path pointing to folder containing JDBC driver scripts.
     */
    public static final String KIE_JDBC_DRIVER_SCRIPTS = "kie.jdbc.driver.scripts";

    public static String getOpenShiftUrl() {
        return System.getProperty(OPENSHIFT_URL);
    }

    public static String getOpenShiftUserName() {
        return System.getProperty(OPENSHIFT_USER);
    }

    public static String getOpenShiftPassword() {
        return System.getProperty(OPENSHIFT_PASSWORD);
    }

    public static String getOpenShiftToken() {
        return System.getProperty(OPENSHIFT_TOKEN);
    }

    public static boolean isOpenShiftTokenSet() {
        return getOpenShiftToken() != null && !getOpenShiftToken().isEmpty();
    }

    public static String getOpenShiftAdminUserName() {
        return System.getProperty(OPENSHIFT_ADMIN_USER);
    }

    public static String getOpenShiftAdminPassword() {
        return System.getProperty(OPENSHIFT_ADMIN_PASSWORD);
    }

    public static String getOpenShiftAdminToken() {
        return System.getProperty(OPENSHIFT_ADMIN_TOKEN);
    }

    public static boolean isOpenShiftAdminTokenSet() {
        return getOpenShiftAdminToken() != null && !getOpenShiftAdminToken().isEmpty();
    }

    public static String getOpenShiftVersion() {
        return System.getProperty(OPENSHIFT_VERSION);
    }

    public static Optional<String> getNamespacePrefix() {
        return Optional.ofNullable(System.getProperty(NAMESPACE_PREFIX));
    }

    public static String getTrustedKeystoreFile() {
        return System.getProperty(TRUSTED_KEYSTORE_FILE);
    }

    public static String getKieImageStreams() {
        return System.getProperty(KIE_IMAGE_STREAMS);
    }

    public static String getNexusMirrorImageStream() {
        return System.getProperty(NEXUS_MIRROR_IMAGE_STREAM);
    }

    public static String getRegistryMirrorImageStream() {
        return System.getProperty(REGISTRY_MIRROR_IMAGE_STREAM);
    }

    public static String getSsoImageStreams() {
        return System.getProperty(SSO_IMAGE_STREAMS);
    }

    public static String getPrometheusVersion() {
        return System.getProperty(PROMETHEUS_VERSION);
    }

    public static String getAmqImageStreams() {
        return System.getProperty(AMQ_IMAGE_STREAMS);
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

    public static String getProjectBuildDirectory() {
        return System.getProperty(PROJECT_BUILD_DIRECTORY);
    }

    public static String getHaCepSourcesDir() {
        return System.getProperty(HA_CEP_SOURCES_DIR);
    }

    public static File getKieJdbcDriverScriptsFolder() {
        String kieJdbcDriverScriptsFolderPath = System.getProperty(KIE_JDBC_DRIVER_SCRIPTS);
        File kieJdbcDriverScriptsFolder = new File(kieJdbcDriverScriptsFolderPath);

        if (!kieJdbcDriverScriptsFolder.exists()) {
            throw new RuntimeException("JDBC driver script folder " + kieJdbcDriverScriptsFolder.getAbsolutePath() + " doesn't exist.");
        }

        return kieJdbcDriverScriptsFolder;
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
        if (isOpenShiftTokenSet()) {
            System.setProperty(OpenShiftConfig.OPENSHIFT_MASTER_TOKEN, getOpenShiftToken());
        }
        if (isOpenShiftTokenSet() || isOpenShiftAdminTokenSet()) {
            System.setProperty(OpenShiftConfig.OPENSHIFT_ADMIN_TOKEN, getOpenShiftAdminToken() != null ? getOpenShiftAdminToken() : getOpenShiftToken());
        }
        System.setProperty(OpenShiftConfig.OPENSHIFT_VERSION, getOpenShiftVersion());
        System.setProperty(OpenShiftConfig.OPENSHIFT_NAMESPACE, "openshift");
    }
}

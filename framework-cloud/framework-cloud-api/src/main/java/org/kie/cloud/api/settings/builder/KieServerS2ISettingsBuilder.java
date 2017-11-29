/*
 * Copyright 2017 JBoss by Red Hat.
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
package org.kie.cloud.api.settings.builder;

import org.kie.cloud.api.protocol.Protocol;
import org.kie.cloud.api.settings.DeploymentSettings;

/**
 * Cloud settings builder for Kie Server S2I.
 *
 * If any environment variable isn't configured by SettingsBuilder, then default
 * value from application template is used.
 */
public interface KieServerS2ISettingsBuilder extends SettingsBuilder<DeploymentSettings> {

    /**
     * Return configured builder with application name.
     *
     * @param name Application name.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withApplicationName(String name);

    /**
     * Return configured builder with Kie Server user.
     *
     * @param kieServerUser Kie Server username.
     * @param kieServerPwd Kie Server password.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withKieServerUser(String kieServerUser, String kieServerPwd);

    /**
     * Return configured builder with Controller user.
     *
     * @param controllerUser Controller username.
     * @param controllerPwd Controller password.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withControllerUser(String controllerUser, String controllerPwd);

    /**
     * Return configured builder with connection to Controller.
     *
     * @param protocol Protocol for URL to Controller.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withControllerProtocol(Protocol protocol);

    /**
     * Return configured builder with connection to Controller set by service
     * name.
     *
     * @param serviceName Controller service name.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withControllerConnection(String serviceName);

    /**
     * Return configured builder with connection to Controller.
     *
     * @param url URL to Controller.
     * @param port port of Controller.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withControllerConnection(String url, String port);

    /**
     * Return configured builder with connection to Smart Router.
     *
     * @param url URL to Smart Router.
     * @param port port of Smart Router.
     * @return Builder.
     */
    KieServerS2ISettingsBuilder withSmartRouterConnection(String url, String port);

    /**
     * Return configured builder with connection to Smart Router.
     *
     * @param serviceName Smart Router service name.
     * @return Builder.
     */
    KieServerS2ISettingsBuilder withSmartRouterConnection(String serviceName);

    /**
     * Return configured builder with external database connection. Values for
     * external database are add by system properties.
     *
     * System properties for external database configuration: db.hostname,
     * db.name, db.password, db.username.
     *
     * @return Builder
     */
    KieServerS2ISettingsBuilder withExternalDatabase();

    /**
     * Return configured builder with Kie Container deployment.
     *
     * @param kieContainerDeployment Kie Container deployment.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withContainerDeployment(String kieContainerDeployment);

    /**
     * Return configured builder with Source location
     *
     * @param gitRepoUrl Repository url.
     * @param gitReference Repository reference (branch/tag). E.g. 'master'.
     * @param gitContextDir Repository context (location of pom file).
     * @return Builder
     */
    KieServerS2ISettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir);

    /**
     * Return configured builder with Source location
     *
     * @param gitRepoUrl Repository url.
     * @param gitReference Repository reference (branch/tag). E.g. 'master'.
     * @param gitContextDir Repository context (location of pom file).
     * @param artifactDirs Directories containing built kjars, separated by commas. For example "usertask-project/target,signaltask-project/target".
     * @return Builder
     */
    KieServerS2ISettingsBuilder withSourceLocation(String gitRepoUrl, String gitReference, String gitContextDir, String artifactDirs);

    /**
     * Return configured builder with Maven repository.
     *
     * @param url Address of the maven repository.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withMavenRepoUrl(String url);

    /**
     * Return configured builder with Maven repository set by service name.
     *
     * @param serviceName Service name (e.g. Business central deployment).
     * @param path Path to maven repositoy (e.g. '/maven2/').
     * @return Builder
     */
    KieServerS2ISettingsBuilder withMavenRepoService(String serviceName, String path);

    /**
     * Return configured builder with Maven user.
     *
     * @param repoUser Maven user.
     * @param repoPassword Maven password.
     * @return Builder
     */
    KieServerS2ISettingsBuilder withMavenRepoUser(String repoUser, String repoPassword);

    /**
     * Return configured builder with set Kie Server sync deploying.
     *
     * @param syncDeploy set to true for sync deploy to Kie server
     * @return Builder
     */
    KieServerS2ISettingsBuilder withKieServerSyncDeploy(boolean syncDeploy);

    /**
     * Return configured builder with set host route. Custom hostname for http
     * service route.
     *
     * @param http
     * @return
     */
    KieServerS2ISettingsBuilder withHostame(String http);

    /**
     * Return configured builder with set secured host route. Custom hostname
     * for https service route.
     *
     * @param https
     * @return
     */
    KieServerS2ISettingsBuilder withSecuredHostame(String https);
}

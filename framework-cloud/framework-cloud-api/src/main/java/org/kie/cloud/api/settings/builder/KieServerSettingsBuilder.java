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

import org.kie.cloud.api.settings.DeploymentSettings;

/**
 * Cloud settings builder for Kie Server.
 *
 * If any environment variable isn't configured by SettingsBuilder, then default
 * value from application template is used.
 */
public interface KieServerSettingsBuilder extends SettingsBuilder<DeploymentSettings> {

    /**
     * Return configured builder with application name.
     *
     * @param name Application name.
     * @return Builder
     */
    KieServerSettingsBuilder withApplicationName(String name);

    /**
     * Return configured builder with Kie Server user.
     *
     * @param kieServerUser Kie Server username.
     * @param kieServerPwd Kie Server password.
     * @return Builder
     */
    KieServerSettingsBuilder withKieServerUser(String kieServerUser, String kieServerPwd);

    /**
     * Return configured builder with Kie Admin user.
     *
     * @param user Kie Admin username.
     * @param password Kie Admin password.
     * @return Builder
     */
    KieServerSettingsBuilder withAdminUser(String user, String password);

    /**
     * Return configured builder with Controller user.
     *
     * @param controllerUser Controller username.
     * @param controllerPwd Controller password.
     * @return Builder
     */
    KieServerSettingsBuilder withControllerUser(String controllerUser, String controllerPwd);

    /**
     * Return configured builder with connection to Controller set by service
     * name.
     *
     * @param serviceName Controller service name.
     * @return Builder
     */
    KieServerSettingsBuilder withControllerConnection(String serviceName);

    /**
     * Return configured builder with connection to Controller.
     *
     * @param url URL to Controller.
     * @param port port of Controller URL.
     * @return Builder
     */
    KieServerSettingsBuilder withControllerConnection(String url, String port);

    /**
     * Return configured builder with connection to Controller.
     *
     * @param protocol protocol of Controller URL.
     * @param url URL to Controller.
     * @param port port of Controller URL.
     * @return Builder
     */
    KieServerSettingsBuilder withControllerConnection(String protocol, String url, String port);

    /**
     * Return configured builder with connection to Smart Router.
     *
     * @param url URL to Smart Router.
     * @param port port of Smart Router URL.
     * @return Builder.
     */
    KieServerSettingsBuilder withSmartRouterConnection(String url, String port);

    /**
     * Return configured builder with connection to Smart Router.
     *
     * @param protocol protocol of Smart Router URL.
     * @param url URL to Smart Router.
     * @param port port of Smart Router URL.
     * @return Builder.
     */
    KieServerSettingsBuilder withSmartRouterConnection(String protocol, String url, String port);

    /**
     * Return configured builder with connection to Smart Router.
     *
     * @param serviceName Smart Router service name.
     * @return Builder.
     */
    KieServerSettingsBuilder withSmartRouterConnection(String serviceName);

    /**
     * Return configured builder with Kie Container deployment.
     *
     * @param kieContainerDeployment Kie Container deployment.
     * @return Builder
     */
    KieServerSettingsBuilder withContainerDeployment(String kieContainerDeployment);

    /**
     * Return configured builder with Maven repository.
     *
     * @param url Address of the maven repository.
     * @return Builder
     */
    KieServerSettingsBuilder withMavenRepoUrl(String url);

    /**
     * Return configured builder with Maven repository set by service name.
     *
     * @param serviceName Service name (e.g. Business central deployment).
     * @return Builder
     */
    KieServerSettingsBuilder withMavenRepoService(String serviceName);

    /**
     * Return configured builder with Maven repository set by service name.
     *
     * @param serviceName Service name (e.g. Business central deployment).
     * @param path Path to maven repositoy (e.g. '/maven2/').
     * @return Builder
     */
    KieServerSettingsBuilder withMavenRepoService(String serviceName, String path);

    /**
     * Return configured builder with Maven user for the Maven service.
     *
     * @param workbenchMavenUser
     * @param workbenchMavenPassword
     * @return
     */
    KieServerSettingsBuilder withMavenRepoServiceUser(String workbenchMavenUser, String workbenchMavenPassword);

    /**
     * Return configured builder with Maven user.
     *
     * @param repoUser Maven user.
     * @param repoPassword Maven password.
     * @return Builder
     */
    KieServerSettingsBuilder withMavenRepoUser(String repoUser, String repoPassword);

    /**
     * Return configured builder with set Kie Server sync deploying.
     *
     * @param syncDeploy set to true for sync deploy to Kie server
     * @return Builder
     */
    KieServerSettingsBuilder withKieServerSyncDeploy(boolean syncDeploy);

    /**
     * Return configured builder with set bypass auth for Kie server.
     *
     * @param bypassAuth set to true for bypass auth for Kie server.
     * @return Builder
     */
    KieServerSettingsBuilder withKieServerBypassAuthUser(boolean bypassAuth);

    /**
     * Return configured builder with enbaled drools classes filter for Kie
     * server.
     *
     * @param droolsFilter set to true to enable drools classes filter.
     * @return Builder
     */
    KieServerSettingsBuilder withDroolsServerFilterClasses(boolean droolsFilter);

    /**
     * Return configured builder with set host route. Custom hostname for http
     * service route.
     *
     * @param http
     * @return
     */
    KieServerSettingsBuilder withHostame(String http);

    /**
     * Return configured builder with set secured host route. Custom hostname
     * for https service route.
     *
     * @param https
     * @return
     */
    KieServerSettingsBuilder withSecuredHostame(String https);

    /**
     * Return configured builder with set Kie server HTTPS secret.
     *
     * @param secret
     * @return Builder
     */
    KieServerSettingsBuilder withKieServerSecret(String secret);
}

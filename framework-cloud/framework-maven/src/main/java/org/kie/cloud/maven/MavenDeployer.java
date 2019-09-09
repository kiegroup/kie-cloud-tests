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

package org.kie.cloud.maven;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.it.VerificationException;
import org.kie.cloud.api.constants.ConfigurationInitializer;
import org.kie.cloud.maven.util.MavenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenDeployer {

    private static final Logger logger = LoggerFactory.getLogger(MavenDeployer.class);

    private static final String SETTINGS_XML_PATH_KEY = "kjars.build.settings.xml";

    // Environment variables that can be configured for this deployer
    public static final String MAVEN_DEPLOYER_REPO_URL_KEY = "MAVEN_DEPLOYER_REPO_URL";
    public static final String MAVEN_DEPLOYER_REPO_USERNAME_KEY = "MAVEN_DEPLOYER_REPO_USERNAME";
    public static final String MAVEN_DEPLOYER_REPO_PASSWORD_KEY = "MAVEN_DEPLOYER_REPO_PASSWORD";
    // Keep those for Backward Compatibility with system properties
    public static final String MAVEN_REPO_URL_KEY = "maven.repo.url";
    public static final String MAVEN_REPO_USERNAME_KEY = "maven.repo.username";
    public static final String MAVEN_REPO_PASSWORD_KEY = "maven.repo.password";

    static {
        ConfigurationInitializer.initConfigProperties();
    }

    /**
     * Build Maven project from specified directory using maven command "clean install".
     *
     * @param basedir Directory to build a project from.
     * @param environment Map of key / value environment variables that could be used
     */
    public static void buildAndInstallMavenProject(String basedir, Map<String, String> environment) {
        buildMavenProject(basedir, "install", environment);
    }

    /**
     * Build Maven project from specified directory using maven command "clean deploy".
     *
     * @param basedir Directory to build a project from.
     * @param environment Map of key / value environment variables that could be used
     */
    public static void buildAndDeployMavenProject(String basedir, Map<String, String> environment) {
        buildMavenProject(basedir, "deploy", environment);
    }

    /**
     * Build Maven project from specified directory using maven command from parameter.
     *
     * @param basedir Directory to build a project from.
     * @param buildCommand Build command, for example "install" or "deploy".
     */
    private static void buildMavenProject(String basedir, String buildCommand, Map<String, String> environment) {
        try {
            MavenUtil mavenUtil = MavenUtil.forProject(Paths.get(basedir)).forkJvm();
            addSettingsXmlPathIfExists(mavenUtil, environment);
            addDistributionRepository(mavenUtil, environment);
            environment.entrySet().forEach(e -> mavenUtil.setEnvironmentVariable(e.getKey(), e.getValue()));
            mavenUtil.executeGoals(buildCommand);

            logger.debug("Maven project successfully built and deployed!");
        } catch (VerificationException e) {
            throw new RuntimeException("Error while building Maven project from basedir " + basedir, e);
        }
    }

    /**
     * Add settings.xml file to maven build if it was defined and exists.
     *
     * @param mavenUtil
     */
    private static void addSettingsXmlPathIfExists(MavenUtil mavenUtil, Map<String, String> environment) {
        getSettingsXmlPath(environment).map(Paths::get).ifPresent(settingsXmlPath -> {
            if (settingsXmlPath.toFile().exists()) {
                mavenUtil.useSettingsXml(settingsXmlPath);
            } else {
                throw new RuntimeException("Path to settings.xml file with value " + settingsXmlPath + " points to non existing location.");
            }
        });
    }

    /**
     * Setup the Maven distribution repository information if available as system property. 
     *
     * @param mavenUtil
     */
    private static void addDistributionRepository(MavenUtil mavenUtil, Map<String, String> environment) {
        List<String> cliOptions = new ArrayList<String>();
        getEnvVar(environment, MAVEN_DEPLOYER_REPO_URL_KEY, MAVEN_REPO_URL_KEY)
                .map(url -> constructMavenEnvCliOption(MAVEN_REPO_URL_KEY, url.replaceAll("\\/\\/", "\\/\\/\\/")))
                .ifPresent(cliOptions::add);
        getEnvVar(environment, MAVEN_DEPLOYER_REPO_USERNAME_KEY, MAVEN_REPO_USERNAME_KEY)
                .map(username -> constructMavenEnvCliOption(MAVEN_REPO_USERNAME_KEY, username))
                .ifPresent(cliOptions::add);
        getEnvVar(environment, MAVEN_DEPLOYER_REPO_PASSWORD_KEY, MAVEN_REPO_PASSWORD_KEY)
                .map(password -> constructMavenEnvCliOption(MAVEN_REPO_PASSWORD_KEY, password))
                .ifPresent(cliOptions::add);
        mavenUtil.addCliOptions(cliOptions);
    }

    private static String constructMavenEnvCliOption(String key, String value) {
        return "-D" + key + "=" + value;
    }

    private static Optional<String> getSettingsXmlPath(Map<String, String> environment) {
        return getEnvVar(environment, SETTINGS_XML_PATH_KEY);
    }

    private static Optional<String> getEnvVar(Map<String, String> environment, String envKey) {
        return getEnvVar(environment, envKey, envKey);
    }

    private static Optional<String> getEnvVar(Map<String, String> environment, String envKey, String systemKey) {
        if (environment.containsKey(envKey)) {
            return Optional.of(environment.get(envKey));
        } else {
            return Optional.ofNullable(System.getProperty(systemKey));
        }
    }
}

/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.maven.util;


import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;

public class MavenUtil {
    private final Verifier maven;

    private MavenUtil(Verifier maven) {
        this.maven = maven;
        maven.setForkJvm(false);
    }

    public static MavenUtil forProject(Path projectPath) throws VerificationException {
        Verifier verifier = new Verifier(projectPath.toAbsolutePath().toString());

        return new MavenUtil(verifier);
    }

    public static MavenUtil forProject(Path projectPath, Path settingsXmlPath) throws VerificationException {
        Verifier verifier = new Verifier(projectPath.toAbsolutePath().toString(), settingsXmlPath.toAbsolutePath().toString());

        MavenUtil result = new MavenUtil(verifier);
        result.useSettingsXml(settingsXmlPath);

        return result;
    }

    public MavenUtil useSettingsXml(Path settingsXmlPath) {
        maven.addCliOption("-s " + settingsXmlPath.toAbsolutePath().toString());
        return this;
    }

    public MavenUtil disableAutoclean() {
        maven.setAutoclean(false);
        return this;
    }

    public MavenUtil forkJvm() {
        maven.setForkJvm(true);

        // copy the DNS configuration
        if (System.getProperty("sun.net.spi.nameservice.nameservers") != null) {
            maven.setSystemProperty("sun.net.spi.nameservice.nameservers", System.getProperty("sun.net.spi.nameservice.nameservers"));
            maven.setSystemProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
            maven.setSystemProperty("sun.net.spi.nameservice.provider.2", "default");
        }

        return this;
    }

    public void executeGoals(String... goals) throws VerificationException {
        try {
            maven.executeGoals(Arrays.asList(goals));
        } finally {
            // always reset System.out and System.in streams
            maven.resetStreams();
        }
    }

    public MavenUtil addCliOptions(List<String> options) {
        //use add to avoid override of default options
        options.stream().forEach(maven::addCliOption);
        return this;
    }

    public MavenUtil addCliOptions(String... options) {
        addCliOptions(Arrays.asList(options));
        return this;
    }

    public MavenUtil setEnvironmentVariable(String key, String value) {
        this.maven.setEnvironmentVariable(key, value);
        return this;
    }
}

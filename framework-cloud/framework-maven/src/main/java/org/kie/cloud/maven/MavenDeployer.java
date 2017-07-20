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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.cli.MavenCli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenDeployer {

    private static final Logger logger = LoggerFactory.getLogger(MavenDeployer.class);

    public static void buildAndDeployMavenProject(String basedir) {
        String buildSettings = System.getProperty("kjars.build.settings.xml");

        // need to backup (and later restore) the current class loader, because the Maven/Plexus does some classloader
        // magic which then results in CNFE in RestEasy client
        // run the Maven build which will create the kjar. The kjar is then either installed or deployed to local and
        // remote repo
        logger.debug("Building and deploying Maven project from basedir '{}'.", basedir);
        ClassLoader classLoaderBak = Thread.currentThread().getContextClassLoader();
        System.setProperty("maven.multiModuleProjectDirectory", basedir); // required by MavenCli 3.3.0+

        List<String> mvnArgs = new ArrayList<String>(Arrays.asList("-B", "-e", "clean", "deploy"));;

        // use custom settings.xml file, if one specified
        if (buildSettings != null && !buildSettings.isEmpty()) {
            mvnArgs.add("-s");
            mvnArgs.add(buildSettings);
        }

        MavenCli cli = new MavenCli();
        int mvnRunResult = cli.doMain(mvnArgs.toArray(new String[mvnArgs.size()]), basedir, System.out, System.err);

        Thread.currentThread().setContextClassLoader(classLoaderBak);

        if (mvnRunResult != 0) {
            throw new RuntimeException("Error while building Maven project from basedir " + basedir +
                    ". Return code=" + mvnRunResult);
        }
        logger.debug("Maven project successfully built and deployed!");
    }
}

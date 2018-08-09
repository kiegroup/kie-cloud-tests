/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.cloud.openshift.database.driver;

public class OracleExternalDriver extends AbstractExternalDriver {

    @Override
    public String getImageName() {
        return "oracle-driver-image";
    }

    @Override
    public String getImageVersion() {
        return "12c";
    }

    @Override
    public String getDockerFileRelativePath() {
        return "oracle-driver-image";
    }

    @Override
    public String getCustomInstallDirectories() {
        return "oracle-driver/extensions";
    }

    @Override
    public String getSourceImagePath() {
        return "/extensions:oracle-driver/";
    }

    @Override
    protected String getDriverBinaryFileLocationInArtifactRepo() {
        return "com/oracle/ojdbc7/12.1.0.1/ojdbc7-12.1.0.1.jar";
    }
}

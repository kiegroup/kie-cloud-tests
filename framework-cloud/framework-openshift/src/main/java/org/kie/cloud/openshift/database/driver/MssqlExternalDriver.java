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

public class MssqlExternalDriver extends AbstractExternalDriver {

    @Override
    public String getImageName() {
        return "mssql-driver-image";
    }

    @Override
    public String getImageVersion() {
        return "2016";
    }

    @Override
    public String getDockerFileRelativePath() {
        return "mssql-driver-image";
    }

    @Override
    public String getCustomInstallDirectories() {
        return "mssql-driver/extensions";
    }

    @Override
    public String getSourceImagePath() {
        return "/extensions:mssql-driver/";
    }

    @Override
    protected String getDriverBinaryFileLocationInArtifactRepo() {
        return "com/microsoft/sqlserver/sqljdbc4/4.0/sqljdbc4-4.0.jar";
    }
}

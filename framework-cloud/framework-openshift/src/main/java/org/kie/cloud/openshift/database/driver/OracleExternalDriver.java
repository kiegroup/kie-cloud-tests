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

import java.util.Optional;

import org.kie.cloud.openshift.resource.CloudProperties;

public class OracleExternalDriver extends AbstractExternalDriver {

    @Override
    public String getName() {
        return "oracle";
    }

    @Override
    public String getImageName() {
        return "jboss-kie-oracle-extension-openshift-image";
    }

    @Override
    public String getImageVersion() {
        return "12c";
    }

    @Override
    public Optional<String> getJdbcDriverUrl() {
        return Optional.ofNullable(CloudProperties.getInstance().getOracleJdbcDriverUrl());
    }
}

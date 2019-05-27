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

package org.kie.cloud.openshift.operator.model.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * KieApp upgrades.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Upgrades {

    private Boolean patch;
    private Boolean minor;

    public Boolean getMinor() {
        return minor;
    }

    public void setMinor(Boolean minor) {
        this.minor = minor;
    }

    public Boolean getPatch() {
        return patch;
    }

    public void setPatch(Boolean patch) {
        this.patch = patch;
    }
}

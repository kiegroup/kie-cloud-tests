/*
 * Copyright 2019 JBoss by Red Hat.
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

package org.kie.cloud.openshift.util.operator;

/**
 * Source of OpenShift operators. Operators can come from community, Red Hat or from certified source.
 */
public enum OperatorSource {
    COMMUNITY("community-operators");

    private String name;

    private OperatorSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

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

package org.kie.cloud.openshift.operator.model;

import io.fabric8.kubernetes.client.CustomResource;
import org.kie.cloud.openshift.operator.model.components.Spec;

/**
 * Custom resource representation used by Fabric8 OpenShift client.
 */
public class KieApp extends CustomResource {

    private static final long serialVersionUID = -7608178420952152353L;

    private Spec spec = new Spec();

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

}

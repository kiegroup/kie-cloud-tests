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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.kie.cloud.openshift.operator.model.components.Spec;
import org.kie.cloud.openshift.operator.model.components.Status;

/**
 * Custom resource representation used by Fabric8 OpenShift client.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Group("app.kiegroup.org")
@Version("v2")
public class KieApp extends CustomResource<Spec, Status> implements Namespaced {

    private static final long serialVersionUID = -7608178420952152353L;

    @Override
    protected Spec initSpec() {
        return new Spec();
    }

    @Override
    protected Status initStatus() {
        return new Status();
    }
}

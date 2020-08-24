/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
 * JVM configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Jvm {

    private Integer javaInitialMemRatio;
    private Integer javaMaxMemRatio;

    public Integer getJavaInitialMemRatio() {
        return javaInitialMemRatio;
    }

    public void setJavaInitialMemRatio(Integer javaInitialMemRatio) {
        this.javaInitialMemRatio = javaInitialMemRatio;
    }

    public Integer getJavaMaxMemRatio() {
        return javaMaxMemRatio;
    }

    public void setJavaMaxMemRatio(Integer javaMaxMemRatio) {
        this.javaMaxMemRatio = javaMaxMemRatio;
    }
}

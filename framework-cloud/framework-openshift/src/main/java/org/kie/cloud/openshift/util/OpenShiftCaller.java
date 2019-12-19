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

package org.kie.cloud.openshift.util;

import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.fabric8.kubernetes.client.KubernetesClientException;

public class OpenShiftCaller {

    private OpenShiftCaller() {
        // Util class
    }

    /**
     * Make repeatable calls to OpenShift.
     * Sometimes the call can fail due to marshalling error, this happens when object is deleted while marshalling is in progress. In this case retry the call.
     *
     * @param supplier Supplier of OpenShift calls
     * @return Object returned by supplier.
     */
    public static <T> T repeatableCall(Supplier<T> supplier) {
        // Allow 10 calls at maximum, if the call still fails then return error
        for (int i = 0; i < 10; i++) {
            try {
                return supplier.get();
            } catch (KubernetesClientException e) {
                if (e.getCause() instanceof JsonMappingException) {
                    // OpenShift instability, possibly the resource was deleted while unmarshalling the result, continue with another call
                } else {
                    // Unexpected exception, throw it
                    throw e;
                }
            }
        }
        throw new RuntimeException("Repeated calls hit the JsonMappingException.");
    }
}

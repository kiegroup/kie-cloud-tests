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

package org.kie.cloud.openshift.image;

/**
 * Available image streams of Kie deployments.
 */
public enum ImageStream {

    SMART_ROUTER("jboss-bpmsuite70-smartrouter-openshift:TP"),
    WORKBENCH("jboss-bpmsuite70-businesscentral-openshift:TP"),
    WORKBENCH_MONITORING("jboss-bpmsuite70-businesscentral-monitoring-openshift:TP"),
    KIE_SERVER("jboss-bpmsuite70-executionserver-openshift:TP"),
    STANDALONE_CONTROLLER("jboss-bpmsuite70-standalonecontroller-openshift:TP");

    private String imageStreamName;

    private ImageStream(String imageStreamName) {
        this.imageStreamName = imageStreamName;
    }

    public String getImageStreamName() {
        return imageStreamName;
    }
}

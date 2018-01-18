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

package org.kie.cloud.openshift.resource.impl;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import cz.xtf.openshift.OpenShiftUtil;
import io.fabric8.kubernetes.api.model.KubernetesList;
import org.kie.cloud.openshift.OpenShiftController;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectImpl implements Project {

    private static final Logger logger = LoggerFactory.getLogger(ProjectImpl.class);

    private String projectName;
    private OpenShiftUtil util;

    public ProjectImpl(String projectName) {
        this.projectName = projectName;
        this.util = OpenShiftController.getOpenShiftUtil(projectName);
    }

    @Override
    public String getName() {
        return projectName;
    }

    public OpenShiftUtil getOpenShiftUtil() {
        return util;
    }

    @Override
    public void delete() {
        util.deleteProject();
    }

    @Override
    public void processTemplateAndCreateResources(URL templateUrl, Map<String, String> envVariables) {
        KubernetesList resourceList = util.client().templates().inNamespace(projectName).load(templateUrl).process(envVariables);
        util.client().lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void processTemplateAndCreateResources(InputStream templateInputStream, Map<String, String> envVariables) {
        KubernetesList resourceList = util.client().templates().inNamespace(projectName).load(templateInputStream).process(envVariables);
        util.client().lists().inNamespace(projectName).create(resourceList);
    }

    @Override
    public void createResources(String resourceUrl) {
        try {
            KubernetesList resourceList = util.client().lists().inNamespace(projectName).load(new URL(resourceUrl)).get();
            util.client().lists().inNamespace(projectName).create(resourceList);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed resource URL", e);
        }
    }

    @Override
    public void createResources(InputStream inputStream) {
        KubernetesList resourceList = util.client().lists().inNamespace(projectName).load(inputStream).get();
        util.client().lists().inNamespace(projectName).create(resourceList);
    }

    public void close() {
        try {
            util.close();
        } catch (Exception e) {
            logger.warn("Exception while closing OpenShift client.", e);
        }
    }
}

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

package org.kie.cloud.openshift.deployment;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import cz.xtf.core.waiting.SimpleWaiter;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.kie.cloud.api.deployment.KieServerDeployment;
import org.kie.cloud.openshift.resource.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerDeploymentImpl extends OpenShiftDeployment implements KieServerDeployment {

    private static final Logger logger = LoggerFactory.getLogger(KieServerDeploymentImpl.class);

    private Optional<URL> insecureUrl;
    private Optional<URL> secureUrl;
    private String username;
    private String password;

    private String serviceName;
    private String serviceSuffix = "";

    public KieServerDeploymentImpl(Project project) {
        super(project);
    }

    @Override
    public URL getUrl() {
        return getInsecureUrl().orElseGet(() -> getSecureUrl().orElseThrow(() -> new RuntimeException("No Kie server URL is available.")));
    }

    @Override
    public Optional<URL> getInsecureUrl() {
        if (insecureUrl == null) {
            insecureUrl = getHttpRouteUrl(getServiceName());
        }
        return insecureUrl;
    }

    @Override
    public Optional<URL> getSecureUrl() {
        if (secureUrl == null) {
            secureUrl = getHttpsRouteUrl(getServiceName());
        }
        return secureUrl;
    }

    @Override public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServiceSuffix(String serviceSuffix) {
        this.serviceSuffix = serviceSuffix;
    }

    @Override
    public String getServiceName() {
        if (serviceName == null) {
            serviceName = ServiceUtil.getKieServerServiceName(getOpenShift(), serviceSuffix);
        }
        return serviceName;
    }

    @Override public void waitForScale() {
        super.waitForScale();
        if (getInstances().size() > 0) {
            RouterUtil.waitForRouter(getUrl());
        }
    }

    @Override
    public void waitForContainerRespin() {
        getKieServerConfigMap().ifPresent(config -> {
            logger.info("Config map found, waiting for rollout.");
            waitForRolloutTrigger(config);
            waitForRolloutFinish(config);
            waitForScale();
        });
    }

    /**
     * Wait until rollout required label disappear from config map.
     *
     * @param kieServerDeployment Kie server config map with configuration.
     */
    private void waitForRolloutTrigger(ConfigMap kieServerConfigMap) {
        String rolloutRequiredLabel = "services.server.kie.org/openshift-startup-strategy.rolloutRequired";
        Map<String, String> kieServerConfigMapLabels = kieServerConfigMap.getMetadata().getLabels();

        if (kieServerConfigMapLabels.containsKey(rolloutRequiredLabel)) {
            logger.info("Rollout required label found, waiting for rollout trigger.");
            BooleanSupplier rolloutLabelNotFound = () -> !getKieServerConfigMap().orElseThrow(() -> new RuntimeException("Kie server config map not found."))
                                                                                 .getMetadata()
                                                                                 .getLabels()
                                                                                 .containsKey(rolloutRequiredLabel);
            new SimpleWaiter(rolloutLabelNotFound).timeout(TimeUnit.MINUTES, 1).waitFor();
        }
    }

    /**
     * Wait until temporary rollout config map disappears, marking that new Kie server pod is starting.
     *
     * @param kieServerConfigMap Kie server config map with configuration.
     */
    private void waitForRolloutFinish(ConfigMap kieServerConfigMap) {
        String kieServerIdLabel = "services.server.kie.org/kie-server-id";
        String kieServerId = kieServerConfigMap.getMetadata().getLabels().get(kieServerIdLabel);
        String rolloutInProgressConfigMapName = "kieserver-rollout-in-progress-" + kieServerId;

        if (getOpenShift().getConfigMap(rolloutInProgressConfigMapName) != null) {
            logger.info("Temporary rollout config map found, rollout is in progress.");
            new SimpleWaiter(() -> getOpenShift().getConfigMap(rolloutInProgressConfigMapName) == null).timeout(TimeUnit.MINUTES, 5).waitFor();
        }
    }

    /**
     * @return Kie server config map if exists.
     */
    private Optional<ConfigMap> getKieServerConfigMap() {
        // Expecting the config map to be owned by deployment config with same name as its service. Needs to be adjusted in case this changes!!!!
        return getOpenShift().getConfigMaps().stream().filter(cm -> !cm.getMetadata().getOwnerReferences().isEmpty())
                                                      .filter(cm -> cm.getMetadata().getOwnerReferences().get(0).getName().equals(getServiceName()))
                                                      .findAny();
    }
}

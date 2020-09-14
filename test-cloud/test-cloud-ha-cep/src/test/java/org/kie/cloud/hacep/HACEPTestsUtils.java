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

package org.kie.cloud.hacep;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import org.assertj.core.api.Assertions;
import org.kie.cloud.openshift.resource.Project;
import org.kie.remote.CommonConfig;

public class HACEPTestsUtils {

    public static String leaderPodName(final Project project) {
        final ConfigMap leadersConfigMap = project.getOpenShift().configMaps()
                .withName(HACEPTestsConstants.LEADERS_CONFIG_MAP).get();
        Assertions.assertThat(leadersConfigMap.getData()).containsKey(HACEPTestsConstants.LEADER_POD_KEY);
        final String leaderName = leadersConfigMap.getData().get(HACEPTestsConstants.LEADER_POD_KEY);

        return leaderName;
    }

    public static Pod leaderPod(final Project project) {
        final String name = leaderPodName(project);
        final Pod pod = project.getOpenShift().getPod(name);
        Assertions.assertThat(pod).isNotNull();

        return pod;
    }

    public static Properties getProperties() {
        Properties props = CommonConfig.getStatic();

        try (InputStream is = HACEPTestsUtils.class.getClassLoader().getResourceAsStream("consumer.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to find configuration properties", e);
        }

        return props;
    }
}

package org.kie.cloud.openshift.util;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;

public class OpenshiftInstanceUtil {

    /**
     * Create an instance 
     * @param openShift
     * @param namespace
     * @param pod
     * @return
     */
    public static OpenShiftInstance createInstance(OpenShift openShift, String namespace, Pod pod) {
        String instanceName = pod.getMetadata().getName();
        return new OpenShiftInstance(openShift, namespace, instanceName);
    }
}

package org.kie.cloud.openshift.util;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;

public class OpenshiftPodUtil {

    public static final String POD_STATUS_PENDING = "Pending";

    public static Instance createInstance(OpenShift openShift, String namespace, Pod pod) {
        String instanceName = pod.getMetadata().getName();
        return new OpenShiftInstance(openShift, namespace, instanceName);
    }

    public static boolean isRunningPod(Pod pod) {
        return !POD_STATUS_PENDING.equals(pod.getStatus().getPhase());
    }
}

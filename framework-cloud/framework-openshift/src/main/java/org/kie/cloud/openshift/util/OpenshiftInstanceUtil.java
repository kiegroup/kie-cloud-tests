package org.kie.cloud.openshift.util;

import java.util.List;

import cz.xtf.core.openshift.OpenShift;
import io.fabric8.kubernetes.api.model.Pod;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;

import static java.util.stream.Collectors.toList;

public class OpenshiftInstanceUtil {

    public static final String POD_STATUS_PENDING = "Pending";

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

    /**
     * Return list of all started instances in the project.
     *
     * @return List of Instances
     * @see Instance
     */
    public static List<OpenShiftInstance> getAllInstances(OpenShift openshift, String namespace) {
        return openshift.getPods()
                        .stream()
                        .map(pod -> createInstance(openshift, openshift.getNamespace(), pod))
                        .filter(Instance::isRunning)
                        .collect(toList());
    }
}

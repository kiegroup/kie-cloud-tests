package org.kie.cloud.openshift.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.openshift.OpenShift;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;
import org.kie.cloud.openshift.util.OpenshiftPodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstancesLogCollectorRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftScenario.class);

    private static final Integer DEFAULT_OBERVABLE_BUFFER_IN_SECONDS = 5;

    private Project project;
    private String logFolderName;

    private Map<Instance, Future<?>> observedInstances = new ConcurrentHashMap<>();

    public InstancesLogCollectorRunnable(Project project, String logFolderName) {
        super();
        this.project = project;
        this.logFolderName = logFolderName;
    }

    @Override
    public void run() {
        // Check for new instances and oberve on them
        project.getAllInstances().stream()
               // Filter non observed instances
               .filter(instance -> !isInstanceObserved(instance))
               // We only want Openshift instance here
               .filter(instance -> instance instanceof OpenShiftInstance).map(instance -> (OpenShiftInstance) instance)
               // Observe instance logs
               .forEach(this::observeInstanceLog);

        // Check if instances are still existing
        // Calling get pod on a no more existing pod will cause the "obervePodLog" to complete ...
        new ArrayList<>(observedInstances.keySet()).stream()
                                                   .forEach(instance -> project.getPod(instance.getName()));
    }

    public void flushRemainingInstanceLogs() {
        // For all remaining observed instances, stop thread && writeinstancelogs
        List<Instance> instances = new ArrayList<>(observedInstances.keySet());
        instances.forEach(instance -> {
            // Flush all logs
            if (OpenshiftPodUtil.isPodExisting(project.getOpenShift(), instance.getName())) {
                logger.info("Flush logs from {}", instance.getName());
                InstanceLogUtil.writeInstanceLogs(instance, logFolderName);
            } else {
                logger.info("Ignoring instance {} as not running", instance.getName());
            }
        });
        observedInstances.entrySet().forEach(entry -> {
            Future<?> future = entry.getValue();
            // Stop thread
            try {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            } catch (Exception e) {
                logger.error("Error while stopping observable thread from instance " + entry.getKey().getName(), e);
            }

        });
        observedInstances.clear();
    }

    private void observeInstanceLog(OpenShiftInstance instance) {
        OpenShift openshift = instance.getOpenShift();
        Future<?> future = Executors.newSingleThreadExecutor().submit(() -> {
            openshift.observePodLog(openshift.getPod(instance.getName())).buffer(5, TimeUnit.SECONDS)
                     .subscribe(logLines -> instanceLogLines(instance, logLines), error -> {
                         throw new RuntimeException(error);
                     }, () -> removeInstanceObserved(instance));
            removeInstanceObserved(instance);
        });
        setInstanceAsObserved(instance, future);
    }

    private void instanceLogLines(OpenShiftInstance instance, Collection<String> logLines) {
        //		logger.info("Instance {}\n {}", instance.getName(), logLines);
        InstanceLogUtil.appendInstanceLogLines(instance.getName(), logLines, logFolderName);
    }

    private boolean isInstanceObserved(Instance instance) {
        return this.observedInstances.keySet().stream().map(Instance::getName)
                                     .anyMatch(name -> name.equals(instance.getName()));
    }

    private void setInstanceAsObserved(Instance instance, Future<?> future) {
        logger.info("Observe instance {}", instance.getName());
        this.observedInstances.put(instance, future);
    }

    private void removeInstanceObserved(Instance instance) {
        logger.info("finished observing instance {}", instance.getName());
        this.observedInstances.remove(instance);
    }

}

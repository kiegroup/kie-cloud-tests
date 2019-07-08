package org.kie.cloud.openshift.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;
import org.kie.cloud.openshift.util.OpenshiftInstanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstancesLogCollectorRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftScenario.class);

    private static final Integer DEFAULT_OBERVABLE_BUFFER_IN_SECONDS = 5;

    private Project project;
    private String logFolderName;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Set<OpenShiftInstance> observedInstances = Collections.synchronizedSet(new HashSet<>());

    public InstancesLogCollectorRunnable(Project project, String logFolderName) {
        super();
        this.project = project;
        this.logFolderName = logFolderName;
    }

    @Override
    public void run() {
        // Check for new instances and observe on them
        OpenshiftInstanceUtil.getAllInstances(project.getOpenShift(), project.getName()).stream()
                             // Filter non observed instances
                             .filter(instance -> !isInstanceObserved(instance))
                             // Observe instance logs
                             .forEach(this::observeInstanceLog);

        // Check if instances are still existing
        // Calling get pod on a no more existing pod will cause the "obervePodLog" to complete ...
    }

    public void closeAndFlushRemainingInstanceCollectors() {
        // Keep instances list before stopping everything
        List<OpenShiftInstance> instances = new ArrayList<>(observedInstances);
        // Stop all observations
        executorService.shutdownNow();
        // Flush all remaining instances (which were still observed)
        instances.forEach(this::flushInstanceLogs);
    }

    private void observeInstanceLog(OpenShiftInstance instance) {
        Future<?> future = executorService.submit(() -> {
            instance.observeLogs().buffer(DEFAULT_OBERVABLE_BUFFER_IN_SECONDS, TimeUnit.SECONDS)
                    .subscribe(logLines -> instanceLogLines(instance, logLines), error -> {
                        throw new RuntimeException(error);
                    }, () -> removeInstanceObserved(instance));
            removeInstanceObserved(instance);
        });
        setInstanceAsObserved(instance, future);
    }

    private void instanceLogLines(OpenShiftInstance instance, Collection<String> logLines) {
        InstanceLogUtil.appendInstanceLogLines(instance.getName(), logLines, logFolderName);
    }

    private void flushInstanceLogs(Instance instance) {
        logger.trace("Flushing logs from {}", instance.getName());
        if (instance.exists()) {
            logger.trace("Flush logs from {}", instance.getName());
            InstanceLogUtil.writeInstanceLogs(instance, logFolderName);
        } else {
            logger.trace("Ignoring instance {} as not running", instance.getName());
        }
    }

    private boolean isInstanceObserved(OpenShiftInstance instance) {
        return this.observedInstances.stream().map(Instance::getName)
                                     .anyMatch(name -> name.equals(instance.getName()));
    }

    private void setInstanceAsObserved(OpenShiftInstance instance, Future<?> future) {
        logger.trace("Observe instance {}", instance.getName());
        this.observedInstances.add(instance);
    }

    private void removeInstanceObserved(OpenShiftInstance instance) {
        logger.trace("finished observing instance {}", instance.getName());
        this.observedInstances.remove(instance);
    }

}

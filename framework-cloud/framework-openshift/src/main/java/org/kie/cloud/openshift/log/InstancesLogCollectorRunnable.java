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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstancesLogCollectorRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InstancesLogCollectorRunnable.class);

    private static final Integer DEFAULT_OBERVABLE_BUFFER_IN_SECONDS = 5;

    private Project project;
    private String logFolderName;

    protected ExecutorService executorService = Executors.newCachedThreadPool();
    protected Set<OpenShiftInstance> observedInstances = Collections.synchronizedSet(new HashSet<>());

    public InstancesLogCollectorRunnable(Project project, String logFolderName) {
        super();
        this.project = project;
        this.logFolderName = logFolderName;
    }

    @Override
    public void run() {
        // Check for new instances and observe on them
        project.getAllInstances()
               .stream()
               .filter(instance -> instance instanceof OpenShiftInstance)
               .map(instance -> (OpenShiftInstance) instance)
               // Filter non observed instances
               .filter(instance -> !isInstanceObserved(instance))
               // Observe instance logs
               .forEach(this::observeInstanceLog);
    }

    public void closeAndFlushRemainingInstanceCollectors(int waitForCompletionInMs) {
        // Make a copy before stopping collector threads
        List<OpenShiftInstance> instances = new ArrayList<>(observedInstances);

        // Stop all collectors
        executorService.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(waitForCompletionInMs, TimeUnit.MILLISECONDS)) {
                logger.warn("Log collector Threadpool cannot stop. Force shutdown ...");
                executorService.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executorService.awaitTermination(waitForCompletionInMs, TimeUnit.MILLISECONDS))
                    logger.error("Log collector Threadpool did not terminate");
            } else {
                logger.debug("Log collector Threadpool stopped correctly");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executorService.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        } finally {
            // Finally, flush logs to be sure we have the last state of running pods
            instances.forEach(this::flushInstanceLogs);
        }
    }

    private void observeInstanceLog(OpenShiftInstance instance) {
        Future<?> future = executorService.submit(() -> {
            try {
                instance.observeAllContainersLogs()
                        .entrySet()
                        .forEach(entry -> {
                            entry.getValue().buffer(DEFAULT_OBERVABLE_BUFFER_IN_SECONDS, TimeUnit.SECONDS)
                                 .subscribe(logLines -> instanceLogLines(instance, entry.getKey(), logLines), error -> {
                                     throw new RuntimeException(error);
                                 });
                        });

            } catch (Exception e) {
                logger.error("Problem observing logs for instance " + instance.getName(), e);
            } finally {
                removeInstanceObserved(instance);
            }
        });
        setInstanceAsObserved(instance, future);
    }

    private void instanceLogLines(OpenShiftInstance instance, String containerName, Collection<String> logLines) {
        logger.trace("Write log lines {}", logLines);
        InstanceLogUtil.appendInstanceLogLines(getName(instance, containerName), logFolderName, logLines);
    }

    private void flushInstanceLogs(OpenShiftInstance instance) {
        logger.trace("Flushing logs from {}", instance.getName());
        if (instance.exists()) {
            logger.trace("Flush logs from {}", instance.getName());
            instance.getAllContainerLogs()
                    .entrySet()
                    .forEach(entry -> {
                        writeInstanceLogs(instance, entry.getKey(), entry.getValue());
                    });

        } else {
            logger.trace("Ignoring instance {} as not running", instance.getName());
        }
    }

    private void writeInstanceLogs(OpenShiftInstance instance, String containerName, String logs) {
        logger.trace("Write log lines {}", logs);
        InstanceLogUtil.writeInstanceLogs(getName(instance, containerName), logFolderName, logs);
    }

    private boolean isInstanceObserved(OpenShiftInstance instance) {
        synchronized (observedInstances) {
            return this.observedInstances.stream().map(Instance::getName)
                                         .anyMatch(name -> name.equals(instance.getName()));
        }
    }

    private void setInstanceAsObserved(OpenShiftInstance instance, Future<?> future) {
        logger.trace("Observe instance {}", instance.getName());
        this.observedInstances.add(instance);
    }

    private void removeInstanceObserved(OpenShiftInstance instance) {
        logger.trace("finished observing instance {}", instance.getName());
        this.observedInstances.remove(instance);
    }

    private static String getName(OpenShiftInstance instance, String containerName) {
        return instance.getName() + "-" + containerName;
    }

}

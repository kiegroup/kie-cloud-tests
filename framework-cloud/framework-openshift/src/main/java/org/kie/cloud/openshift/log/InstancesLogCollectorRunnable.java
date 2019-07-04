package org.kie.cloud.openshift.log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.xtf.core.openshift.OpenShift;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.common.logs.InstanceLogUtil;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;
import org.kie.cloud.openshift.resource.Project;
import org.kie.cloud.openshift.scenario.OpenShiftScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstancesLogCollectorRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OpenShiftScenario.class);

    private Project project;
    private String logFolderName;

    private Map<String, Future<?>> observedInstances = new HashMap<>();

    public InstancesLogCollectorRunnable(Project project, String logFolderName) {
        super();
        this.project = project;
        this.logFolderName = logFolderName;
    }

    @Override
    public void run() {
        project.getAllInstances().stream()
               // Filter non observed instances
               .filter(instance -> !isInstanceObserved(instance))
               // We only want Openshift instance here
               .filter(instance -> instance instanceof OpenShiftInstance).map(instance -> (OpenShiftInstance) instance)
               // Observe instance logs
               .forEach(this::observeInstanceLog);
    }

    public void releaseLogs() {
        observedInstances.entrySet().stream().filter(e -> !e.getValue().isDone())
                         .forEach(entry -> releaseLog(entry.getKey(), entry.getValue()));
    }

    private void releaseLog(String name, Future<?> future) {
        try {
            future.cancel(false);
        } catch (Exception e) {
            logger.error("Error while stopping log collector on instance " + name, e);
        }
    }

    private void observeInstanceLog(OpenShiftInstance instance) {
        OpenShift openshift = instance.getOpenShift();
        Future<?> future = Executors.newSingleThreadExecutor().submit(() -> {
            openshift.observePodLog(openshift.getPod(instance.getName())).buffer(5, TimeUnit.SECONDS)
                     .subscribe(logLines -> instanceLogLines(instance, logLines), error -> {
                         throw new RuntimeException(error);
                     }, () -> removeInstanceObserved(instance));
        });
        setInstanceAsObserved(instance, future);
    }

    private void instanceLogLines(OpenShiftInstance instance, Collection<String> logLines) {
        //		logger.info("Instance {}\n {}", instance.getName(), logLines);
        InstanceLogUtil.appendInstanceLogLines(instance.getName(), logLines, logFolderName);
    }

    private boolean isInstanceObserved(Instance instance) {
        return this.observedInstances.containsKey(instance.getName());
    }

    private void setInstanceAsObserved(Instance instance, Future<?> future) {
        logger.info("Observe instance {}", instance.getName());
        this.observedInstances.put(instance.getName(), future);
    }

    private void removeInstanceObserved(Instance instance) {
        logger.info("finished observing instance {}", instance.getName());
        this.observedInstances.remove(instance.getName());
    }

}

package org.kie.cloud.openshift.log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.cloud.api.deployment.Instance;
import org.kie.cloud.openshift.deployment.OpenShiftInstance;
import org.kie.cloud.openshift.resource.Project;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class InstancesLogCollectorRunnableTest {

    private static final String LOG_FOLDER_NAME = "LOG_FOLDER_NAME";
    private static final String PROJECT_NAME = "PROJECT_NAME";
    private static final String LOG_OUTPUT_DIRECTORY = "instances";
    private static final String LOG_SUFFIX = ".log";

    private static final Integer DEFAULT_WAIT_FOR_COMPLETION_IN_MS = 5000;

    @Mock
    Project projectMock;

    InstancesLogCollectorRunnable cut;

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(LOG_OUTPUT_DIRECTORY));

        Mockito.when(projectMock.getName()).thenReturn(PROJECT_NAME);

        cut = new InstancesLogCollectorRunnable(projectMock, LOG_FOLDER_NAME);
    }

    @Test
    public void oneInstanceRunning() {
        setObserveLogCallable(setInstanceMocks("BONJOUR"), null);
        ExecutorService executorService = retrieveExecutorService();

        cut.run();

        assertEquals(1, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances("BONJOUR");

        cut.closeAndFlushRemainingInstanceCollectors(DEFAULT_WAIT_FOR_COMPLETION_IN_MS);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances();

        checkLog("BONJOUR", true);

    }

    @Test
    public void oneInstanceRunningRunnableExecutedTwice() {
        setObserveLogCallable(setInstanceMocks("BONJOUR"), null);
        ExecutorService executorService = retrieveExecutorService();

        cut.run();
        cut.run();

        assertEquals(1, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances("BONJOUR");

        cut.closeAndFlushRemainingInstanceCollectors(DEFAULT_WAIT_FOR_COMPLETION_IN_MS);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances();

        checkLog("BONJOUR", true);
    }

    @Test
    public void manyInstancesRunning() {
        List<OpenShiftInstance> instances = setInstanceMocks("BONJOUR", "HELLO", "BUON GIORNO", "HALLO", "DOBRY DEN");
        setObserveLogCallable(instances, 1000); // Add small tempo to be sure the check after on the number of threads/observed instances is correct 

        ExecutorService executorService = retrieveExecutorService();

        cut.run();

        assertEquals(5, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances("BONJOUR", "HELLO", "BUON GIORNO", "HALLO", "DOBRY DEN");

        cut.closeAndFlushRemainingInstanceCollectors(DEFAULT_WAIT_FOR_COMPLETION_IN_MS);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances();

        checkLog("BONJOUR", true);
        checkLog("HELLO", true);
        checkLog("BUON GIORNO", true);
        checkLog("HALLO", true);
        checkLog("DOBRY DEN", true);
    }

    @Test
    public void killBeforeFinished() {
        setObserveLogCallable(setInstanceMocks("BONJOUR"), 2000);
        ExecutorService executorService = retrieveExecutorService();

        cut.run();

        assertEquals(1, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances("BONJOUR");

        // Here we wait less than the time for the message to be delivered
        cut.closeAndFlushRemainingInstanceCollectors(1000);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());
        checkObservedInstances();

        checkLog("BONJOUR", false);
    }

    private List<OpenShiftInstance> setInstanceMocks(String... messages) {
        List<OpenShiftInstance> instances = Arrays.asList(messages)
                                                  .stream()
                                                  .map(this::createInstanceMock)
                                                  .collect(Collectors.toList());

        Mockito.when(projectMock.getAllInstances())
               .thenReturn(instances.stream()
                                    .map(inst -> (Instance) inst)
                                    .collect(Collectors.toList()));

        return instances;
    }

    private OpenShiftInstance createInstanceMock(String message) {
        OpenShiftInstance instanceMock = Mockito.mock(OpenShiftInstance.class);
        Mockito.when(instanceMock.getName()).thenReturn(message);
        return instanceMock;
    }

    private void setObserveLogCallable(List<OpenShiftInstance> instances, Integer waitForMessage) {
        instances.forEach(instance -> {
            Mockito.when(instance.observeLogs()).then((invocation) -> {
                return Observable.fromCallable(() -> {
                    if (Objects.nonNull(waitForMessage)) {
                        Thread.sleep(waitForMessage);
                    }
                    return instance.getName();
                });
            });
        });
    }

    private ExecutorService retrieveExecutorService() {
        return cut.executorService;
    }

    private void checkObservedInstances(String... instanceNames) {
        assertEquals(instanceNames.length, cut.observedInstances.size());
        Arrays.asList(instanceNames).forEach(instanceName -> {
            assertTrue("Instance with name " + instanceName + " is not observed...", cut.observedInstances.stream().map(Instance::getName).anyMatch(instanceName::equals));
        });
    }

    private void checkLog(String message, boolean exist) {
        assertEquals(exist, isLogExisting(message));
        if (exist) {
            assertEquals("Log for " + message + "is wrong", message, readLog(message));
        }
    }

    private static boolean isLogExisting(String instanceName) {
        return getOutputFile(instanceName).exists();
    }

    private static String readLog(String instanceName) {
        try {
            return FileUtils.readFileToString(getOutputFile(instanceName), "UTF-8").trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getOutputFile(String instanceName) {
        File outputDirectory = new File(LOG_OUTPUT_DIRECTORY, LOG_FOLDER_NAME);
        outputDirectory.mkdirs();
        return new File(outputDirectory, instanceName + LOG_SUFFIX);
    }
}

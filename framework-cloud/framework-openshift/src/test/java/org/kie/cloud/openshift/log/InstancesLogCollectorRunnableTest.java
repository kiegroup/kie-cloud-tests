package org.kie.cloud.openshift.log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class InstancesLogCollectorRunnableTest {

    private static final String LOG_FOLDER_NAME = "LOG_FOLDER_NAME";
    private static final String PROJECT_NAME = "PROJECT_NAME";
    private static final String LOG_OUTPUT_DIRECTORY = "instances";
    private static final String LOG_SUFFIX = ".log";

    private static final Integer WAIT_BEFORE_MSG_IN_MS = 2000;
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
        setInstanceMocks("BONJOUR");
        ExecutorService executorService = retrieveExecutorServiceField();

        cut.run();

        assertEquals(1, ((ThreadPoolExecutor) executorService).getActiveCount());

        cut.closeAndFlushRemainingInstanceCollectors(DEFAULT_WAIT_FOR_COMPLETION_IN_MS);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());

        checkLog("BONJOUR", true);

    }

    @Test
    public void oneInstanceRunningRunnableExecutedTwice() {
        setInstanceMocks("BONJOUR");
        ExecutorService executorService = retrieveExecutorServiceField();

        cut.run();
        cut.run();

        assertEquals(1, ((ThreadPoolExecutor) executorService).getActiveCount());

        cut.closeAndFlushRemainingInstanceCollectors(DEFAULT_WAIT_FOR_COMPLETION_IN_MS);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());

        checkLog("BONJOUR", true);
    }

    @Test
    public void manyInstancesRunning() {
        setInstanceMocks("BONJOUR", "HELLO", "BUON GIORNO", "HALLO", "DOBRY DEN");
        ExecutorService executorService = retrieveExecutorServiceField();

        cut.run();

        assertEquals(5, ((ThreadPoolExecutor) executorService).getActiveCount());

        cut.closeAndFlushRemainingInstanceCollectors(DEFAULT_WAIT_FOR_COMPLETION_IN_MS);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());

        checkLog("BONJOUR", true);
        checkLog("HELLO", true);
        checkLog("BUON GIORNO", true);
        checkLog("HALLO", true);
        checkLog("DOBRY DEN", true);
    }

    @Test
    public void killBeforeFinished() {
        setInstanceMocks("BONJOUR");
        ExecutorService executorService = retrieveExecutorServiceField();

        cut.run();

        assertEquals(1, ((ThreadPoolExecutor) executorService).getActiveCount());

        // Here we wait less than the time for the message to be delivered
        cut.closeAndFlushRemainingInstanceCollectors(1000);

        assertEquals(0, ((ThreadPoolExecutor) executorService).getActiveCount());

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

        Mockito.when(instanceMock.observeLogs()).then((invocation) -> {
            return Observable.fromCallable(() -> {
                Thread.sleep(WAIT_BEFORE_MSG_IN_MS);
                return message;
            });
        });
        return instanceMock;
    }

    private ExecutorService retrieveExecutorServiceField() {
        ExecutorService executor;
        try {
            Field field = cut.getClass().getDeclaredField("executorService");
            field.setAccessible(true);
            executor = (ExecutorService) field.get(cut);
            return executor;
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            assertFalse("Unable to retrieve Executor Service", true);
            return null;
        }
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

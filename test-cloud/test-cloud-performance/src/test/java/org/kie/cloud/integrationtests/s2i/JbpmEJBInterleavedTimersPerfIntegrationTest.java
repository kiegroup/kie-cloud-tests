/*
 * Copyright 2019 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.cloud.integrationtests.s2i;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.cloud.tests.common.time.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class JbpmEJBInterleavedTimersPerfIntegrationTest extends BaseJbpmEJBTimersPerfIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseJbpmEJBTimersPerfIntegrationTest.class);

    private static final String CSV_FILE = "./interleaveTimer__"+PROCESSES_COUNT+"_processes__"+SCALE_COUNT+"_pods.csv";
    
    private static final int BATCH_COUNT = Integer.parseInt(System.getProperty("batchCount", "5"));
    private static final int BATCH_SIZE = PROCESSES_COUNT / BATCH_COUNT;
    private static final int BATCH_DELAY = Integer.parseInt(System.getProperty("batchDelay", "10"));
    private static final int TIMER_DELAY = Integer.parseInt(System.getProperty("timerDelay", "1"));
    private static final int BATCH_MAX_TIME = Integer.parseInt(System.getProperty("batchMaxTime", "20"));
    
    protected static final int STARTING_THREADS_COUNT = 20;

    protected static final int PROCESSES_PER_THREAD = BATCH_SIZE / STARTING_THREADS_COUNT;
    
    protected String[] startingTime = new String[BATCH_COUNT];
    protected String[] processTime = new String[BATCH_COUNT];
    
    @Override
    protected void writeCSV() throws IOException {
        ArrayList<Object> record = new ArrayList<Object>();
        try (
               CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(Paths.get(CSV_FILE).toAbsolutePath().toFile(), true), 
                        CSVFormat.DEFAULT);
            ) {
             record.add(Instant.now());
             record.add(PROCESSES_COUNT); 
             record.add(STARTING_THREADS_COUNT);
             for (int i = 0; i < BATCH_COUNT; i++) {
                 logger.info("Recording batch no. {}:  {} - {}", i, startingTime[i], processTime[i]);
                 record.add(startingTime[i].substring(2));
                 record.add(processTime[i].substring(2));
             }
             record.add(HEAP);
             record.addAll(completedHostNameDistribution.values());
             csvPrinter.printRecord(record);
             csvPrinter.flush();            
            }
        
    }

    @Override
    protected void runSingleScenario() {
        logger.info("Starting {} batches", BATCH_COUNT);
        for (int i = 0; i < BATCH_COUNT;) {
            logger.info("Starting batch no. {}", i);

            logger.info("Starting {} processes", BATCH_SIZE);

            Instant startTime = Instant.now();
            // This is just to provide virtually "infinite" time to finish all iterations, i.e. maximum possible duration minus 1 hour,
            // so we can be sure there won't be overflow
            Duration maxDuration = Duration.between(startTime.plus(1, ChronoUnit.HOURS), Instant.MAX);
            Map<String, Object> params = Collections.singletonMap("timerDelay", TIMER_DELAY);

            startAndWaitForStartingThreads(STARTING_THREADS_COUNT, maxDuration, PROCESSES_PER_THREAD, getStartingRunnable(CONTAINER_ID, ONE_TIMER_DURATION_PROCESS_ID, params));
            
            startingTime[i] = Duration.between(startTime, Instant.now()).toString();
            logger.info("Starting processes took: {}", startingTime[i]);

            Duration waitForCompletionDuration = Duration.of(BATCH_MAX_TIME, ChronoUnit.MINUTES);
            logger.info("Batch created. Waiting for a batch to be processed, max waiting time is {}", waitForCompletionDuration);
            waitForAllProcessesToComplete(waitForCompletionDuration);

            processTime[i] = Duration.between(startTime, Instant.now()).toString();
            logger.info("Batch no. {} processed, took approximately {}", i, processTime[i]);

            if (++i < BATCH_COUNT) {
                logger.info("Waiting for another batch to be run in {} seconds", BATCH_DELAY);
                TimeUtils.wait(Duration.of(BATCH_DELAY, ChronoUnit.SECONDS));
            }
        }
    }
    
}

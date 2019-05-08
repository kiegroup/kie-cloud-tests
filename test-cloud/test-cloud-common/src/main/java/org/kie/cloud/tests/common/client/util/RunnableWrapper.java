package org.kie.cloud.tests.common.client.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Wrapper of Runnable we want to run for a fixed period of time
 * or a fixed number of iterations, what finishes first
 */
public class RunnableWrapper implements Runnable {

    private Duration duration;

    private Integer iterations;

    private Runnable codeToRun;

    public RunnableWrapper() {
    }

    public RunnableWrapper(Duration duration, Integer iterations, Runnable codeToRun) {
        this.duration = duration;
        this.iterations = iterations;
        this.codeToRun = codeToRun;
    }

    @Override
    public void run() {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(duration);
        Long i = 0L;
        while (Instant.now().isBefore(endTime) && i < iterations) {
            codeToRun.run();
            i++;
        }
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Integer getIterations() {
        return iterations;
    }

    public void setIterations(Integer iterations) {
        this.iterations = iterations;
    }

    public Runnable getCodeToRun() {
        return codeToRun;
    }

    public void setCodeToRun(Runnable codeToRun) {
        this.codeToRun = codeToRun;
    }
}

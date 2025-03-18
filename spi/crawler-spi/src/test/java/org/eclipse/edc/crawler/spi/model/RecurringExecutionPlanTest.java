package org.eclipse.edc.crawler.spi.model;

import org.eclipse.edc.spi.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class RecurringExecutionPlanTest {
    private static final Integer INTERVAL = 300;
    private RecurringExecutionPlan recurringExecutionPlan;
    private Monitor monitor;

    @BeforeEach
    public void setUp() {
        monitor = mock(Monitor.class);
        Duration schedule = Duration.ofMillis(100);
        Duration initialDelay = Duration.ofMillis(50);
        recurringExecutionPlan = new RecurringExecutionPlan(schedule, initialDelay, monitor);
    }

    @Test
    public void runPlan_shouldExecuteAtLeastOnce() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        Runnable task = counter::incrementAndGet;

        recurringExecutionPlan.run(task);
        Thread.sleep(INTERVAL);
        recurringExecutionPlan.stop();

        assertNotEquals(0, counter.get(), "Task should have been executed at least once");
    }

    @Test
    public void stopPlanWithoutRun_shouldNotLogWarningsOrErrors() {
        recurringExecutionPlan.stop();

        verify(monitor, never()).warning(anyString());
        verify(monitor, never()).severe(anyString(), any(Throwable.class));
    }

    @Test
    public void runPlaneWithException_shouldLogError() throws InterruptedException {
        Runnable task = () -> {
            throw new RuntimeException("Test Exception");
        };

        recurringExecutionPlan.run(task);
        Thread.sleep(INTERVAL);
        recurringExecutionPlan.stop();

        verify(monitor, atLeastOnce()).severe(eq(RecurringExecutionPlan.ERROR_DURING_PLAN_EXECUTION), any(Throwable.class));
    }

    @Test
    public void stopPlan_shouldPreventFurtherPlanExecution() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        Runnable task = counter::incrementAndGet;

        recurringExecutionPlan.run(task);
        Thread.sleep(INTERVAL);
        recurringExecutionPlan.stop();

        int countAfterStop = counter.get();
        Thread.sleep(INTERVAL);

        assertEquals(countAfterStop, counter.get(), "Task should not execute further after stop");
    }

}
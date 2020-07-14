package org.odk.collect.android.support;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.CoroutineAndWorkManagerScheduler;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestScheduler implements Scheduler {

    private final Scheduler wrappedScheduler;

    private final Object lock = new Object();
    private int tasks;
    private Runnable finishedCallback;

    private final Map<String, TaskSpec> taggedWork = new HashMap<>();

    public TestScheduler() {
        WorkManager workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext());
        this.wrappedScheduler = new CoroutineAndWorkManagerScheduler(workManager);
    }

    @Override
    public Cancellable foregroundImmediate(@NotNull Runnable foreground, long repeatPeriod) {
        increment();

        return wrappedScheduler.foregroundImmediate(() -> {
            foreground.run();
            decrement();
        }, repeatPeriod);
    }

    @Override
    public <T> void immediate(@NotNull Supplier<T> foreground, @NotNull Consumer<T> background) {
        increment();

        wrappedScheduler.immediate(foreground, t -> {
            background.accept(t);
            decrement();
        });
    }

    @Override
    public void networkDeferred(@NotNull String tag, @NotNull TaskSpec spec, long repeatPeriod) {
        taggedWork.put(tag, spec);
    }

    @Override
    public void cancelDeferred(@NotNull String tag) {
        taggedWork.remove(tag);
    }

    @Override
    public boolean isRunning(@NotNull String tag) {
        return wrappedScheduler.isRunning(tag);
    }

    public void runDeferredTasks() {
        Context applicationContext = ApplicationProvider.getApplicationContext();

        for (TaskSpec taskSpec : taggedWork.values()) {
            taskSpec.getTask(applicationContext).run();
        }
    }

    public void setFinishedCallback(Runnable callback) {
        this.finishedCallback = callback;
    }

    private void increment() {
        synchronized (lock) {
            tasks++;
        }
    }

    private void decrement() {
        synchronized (lock) {
            tasks--;

            if (tasks == 0 && finishedCallback != null) {
                finishedCallback.run();
            }
        }
    }

    public int getTaskCount() {
        synchronized (lock) {
            return tasks;
        }
    }
}

package org.odk.collect.android.support;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FakeScheduler implements Scheduler {

    private Runnable foregroundTask;
    private Runnable backgroundTask;
    private Boolean cancelled = false;

    @Override
    public <T> void runInBackground(Supplier<T> task, Consumer<T> callback) {
        backgroundTask = () -> callback.accept(task.get());
    }

    @Override
    public void scheduleInBackgroundWhenNetworkAvailable(@NotNull String tag, @NotNull TaskSpec taskSpec, long repeatPeriod) {

    }

    @Override
    public Cancellable schedule(Runnable task, long repeatPeriod) {
        this.foregroundTask = task;
        return () -> {
            cancelled = true;
            return true;
        };
    }

    public void runTask() {
        foregroundTask.run();
    }

    public void runBackgroundTask() {
        if (backgroundTask == null) {
            return;
        }

        backgroundTask.run();
    }

    public Boolean hasBeenCancelled() {
        return cancelled;
    }

    @Override
    public boolean isRunning(@NotNull String tag) {
        return false;
    }

    @Override
    public void cancelInBackground(@NotNull String tag) {

    }
}

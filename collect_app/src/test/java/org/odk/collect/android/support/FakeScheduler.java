package org.odk.collect.android.support;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.Work;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FakeScheduler implements Scheduler {

    private Runnable foregroundTask;
    private Runnable backgroundTask;
    private Boolean cancelled = false;

    @Override
    public <T> void scheduleInBackground(Supplier<T> task, Consumer<T> callback) {
        backgroundTask = () -> callback.accept(task.get());
    }

    @Override
    public <T extends Work> void scheduleInBackground(@NotNull String tag, @NotNull Class<T> workClass, long repeatPeriod) {

    }

    @Override
    public Cancellable scheduleInForeground(Runnable task, long repeatPeriod) {
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
        backgroundTask.run();
    }

    public Boolean hasBeenCancelled() {
        return cancelled;
    }

    @Override
    public boolean isRunning(@NotNull String tag) {
        return false;
    }
}

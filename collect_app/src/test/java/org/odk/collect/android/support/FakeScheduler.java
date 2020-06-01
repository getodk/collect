package org.odk.collect.android.support;

import org.odk.collect.utilities.Cancellable;
import org.odk.collect.utilities.Consumer;
import org.odk.collect.utilities.Scheduler;
import org.odk.collect.utilities.Supplier;

public class FakeScheduler implements Scheduler {

    private Runnable foregroundTask;
    private Runnable backgroundTask;
    private Boolean cancelled = false;

    @Override
    public <T> void scheduleInBackground(Supplier<T> task, Consumer<T> callback) {
        backgroundTask = () -> callback.accept(task.get());
    }

    @Override
    public Cancellable schedule(Runnable task, long period) {
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
}

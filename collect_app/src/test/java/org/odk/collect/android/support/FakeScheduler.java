package org.odk.collect.android.support;

import org.odk.collect.utilities.Cancellable;
import org.odk.collect.utilities.Scheduler;

public class FakeScheduler implements Scheduler {

    private Runnable task;
    private Boolean cancelled = false;

    @Override
    public Cancellable schedule(Runnable task, long period) {
        this.task = task;
        return () -> {
            cancelled = true;
            return true;
        };
    }

    public void runTask() {
        task.run();
    }

    public Boolean isCancelled() {
        return cancelled;
    }
}

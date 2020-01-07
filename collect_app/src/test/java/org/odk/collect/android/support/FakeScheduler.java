package org.odk.collect.android.support;

import org.odk.collect.utilities.Scheduler;

public class FakeScheduler implements Scheduler {

    private Runnable task;
    private Boolean cancelled = false;

    @Override
    public void schedule(Runnable task, long period) {
        this.task = task;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    public void runTask() {
        task.run();
    }

    public Boolean isCancelled() {
        return cancelled;
    }
}

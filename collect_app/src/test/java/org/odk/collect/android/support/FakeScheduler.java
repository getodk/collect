package org.odk.collect.android.support;

import org.odk.collect.android.audio.Scheduler;

public class FakeScheduler implements Scheduler {

    private Runnable task;

    @Override
    public void schedule(Runnable task, long period) {
        this.task = task;
    }

    public void runTask() {
        task.run();
    }
}

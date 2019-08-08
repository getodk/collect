package org.odk.collect.android.audio;

public interface Scheduler {
    void schedule(Runnable task, long period);
}

package org.odk.collect.android.support;

import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CountingScheduler implements Scheduler {

    private final Scheduler wrappedScheduler;

    private final Object lock = new Object();
    private int tasks;
    private Runnable finishedCallback;

    public CountingScheduler(Scheduler wrappedScheduler) {
        this.wrappedScheduler = wrappedScheduler;
    }

    @Override
    public <T> void scheduleInBackground(Supplier<T> task, Consumer<T> callback) {
        increment();

        wrappedScheduler.scheduleInBackground(task, t -> {
            callback.accept(t);
            decrement();
        });
    }

    @Override
    public Cancellable schedule(Runnable task, long period) {
        increment();

        return wrappedScheduler.schedule(() -> {
            task.run();
            decrement();
        }, period);
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

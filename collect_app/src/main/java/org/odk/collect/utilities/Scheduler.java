package org.odk.collect.utilities;

/**
 * Runs tasks in the foreground and background
 */
public interface Scheduler {

    /**
     * * Schedule a task to run in the background (off the UI thread)
     *
     * @param task     the task to be run
     * @param callback run on the UI thread once the task is complete
     */
    <T> void scheduleInBackground(Supplier<T> task, Consumer<T> callback);

    /**
     * * Schedule a task to run and then repeat
     *
     * @param task   the task to be run
     * @param period the period between each run of the task
     * @return object that allows task to be cancelled
     */
    Cancellable schedule(Runnable task, long period);
}

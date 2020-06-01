package org.odk.collect.utilities;

/**
 * An object that schedules tasks to run in the background repeatedly with a fixed interval
 * between runs.
 */
public interface Scheduler {

    /**
     * * Schedule a task to run and then repeat
     *
     * @param task   the task to be run
     * @param period the period between each run of the task
     * @return object that allows task to be cancelled
     */
    Cancellable schedule(Runnable task, long period);
}

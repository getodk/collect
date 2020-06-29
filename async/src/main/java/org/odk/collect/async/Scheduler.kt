package org.odk.collect.async

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Runs tasks in the foreground and background
 */
interface Scheduler {
    /**
     * * Schedule a task to run in the background (off the UI thread)
     *
     * @param task     the task to be run
     * @param callback run on the UI thread once the task is complete
     */
    fun <T> scheduleInBackground(task: Supplier<T>, callback: Consumer<T>)

    /**
     * * Schedule a task to run and then repeat
     *
     * @param task   the task to be run
     * @param period the period between each run of the task
     * @return object that allows task to be cancelled
     */
    fun schedule(task: Runnable, period: Long): Cancellable
}
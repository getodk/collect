package org.odk.collect.async

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Runs tasks in the foreground and background
 */
interface Scheduler {

    /**
     * Schedule a task to run and then repeat
     *
     * @param task   the task to be run
     * @param repeatPeriod the period between each run of the task
     * @return object that allows task to be cancelled
     */
    fun scheduleInForeground(task: Runnable, repeatPeriod: Long): Cancellable

    /**
     * Schedule a task to run in the background (off the UI thread)
     *
     * @param task     the task to be run
     * @param callback run on the foreground once the task is complete
     */
    fun <T> scheduleInBackground(task: Supplier<T>, callback: Consumer<T>)

    /**
     * Returns true if a task scheduled with a tag is currently running
     */
    fun isRunning(tag: String): Boolean
}
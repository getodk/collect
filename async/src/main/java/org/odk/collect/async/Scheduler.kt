package org.odk.collect.async

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Run and schedule tasks in the foreground (UI thread) and background. Based on terminology
 * used in Android's Background Processing documentation: https://developer.android.com/guide/background.
 *
 * This keeps the details of threading and job frameworks (which are very often in flux) away from
 * the UI code. An added advantage of this is the ability to use Kotlin's concurrency primitives from Java.
 */
interface Scheduler {

    /**
     * Run work in the background (off the UI thread) and then use the result of that work
     * in the foreground. Cancelled if application closed.
     *
     * @param background the task to be run
     * @param foreground run on the foreground once the task is complete
     */
    fun <T> immediate(background: Supplier<T>, foreground: Consumer<T>)

    /**
     * Run work in the foreground. Cancelled if application closed.
     */
    fun immediate(foreground: Runnable)

    /**
     * Schedule a task to run in the background even if the app isn't running. The task
     * will only be run when the network is available.
     *
     * @param tag used to identify this task in future
     * @param spec defines the task to be run
     */
    fun networkDeferred(tag: String, spec: TaskSpec)

    /**
     * Schedule a task to run in the background repeatedly even if the app isn't running. The task
     * will only be run when the network is available.
     *
     * @param tag used to identify this task in future. Previously scheduled tasks using the same
     * tag will be replaced
     * @param spec defines the task to be run
     * @param repeatPeriod the period between each run of the task
     */
    fun networkDeferred(tag: String, spec: TaskSpec, repeatPeriod: Long)

    /**
     * Cancel deferred task scheduled with tag
     */
    fun cancelDeferred(tag: String)

    /**
     * Returns true if a deferred task scheduled with tag is currently running
     */
    fun isRunning(tag: String): Boolean

    /**
     * Run a task and then repeat in the foreground
     *
     * @param foreground the task to be run
     * @param repeatPeriod the period between each run of the task
     * @return object that allows task to be cancelled
     */
    fun repeat(foreground: Runnable, repeatPeriod: Long): Cancellable
}

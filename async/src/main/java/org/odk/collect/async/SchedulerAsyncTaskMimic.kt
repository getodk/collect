package org.odk.collect.async

import android.os.AsyncTask

/**
 * Basic reimplementation of the [AsyncTask] API that allows an [AsyncTask] implementation to
 * use [Scheduler] with minimal internal and external changes.
 */
abstract class SchedulerAsyncTaskMimic<Params, Progress, Result>(private val scheduler: Scheduler) {

    @Volatile
    private var status: AsyncTask.Status = AsyncTask.Status.PENDING

    @Volatile
    private var cancelled = false

    protected abstract fun onPreExecute()
    protected abstract fun doInBackground(vararg params: Params): Result
    protected abstract fun onProgressUpdate(vararg values: Progress)
    protected abstract fun onPostExecute(result: Result)
    protected abstract fun onCancelled()

    /**
     * Execute [doInBackground] on calling thread and return the [Result] value. Should probably
     * not be used as a replacement for [AsyncTask.get] (unless it's for testing purposes).
     */
    fun executeSynchronously(vararg params: Params): Result {
        return doInBackground(*params)
    }

    fun execute(vararg params: Params): SchedulerAsyncTaskMimic<Params, Progress, Result> {
        status = AsyncTask.Status.RUNNING
        onPreExecute()

        scheduler.immediate(
            background = {
                doInBackground(*params)
            },
            foreground = { result ->
                if (cancelled) {
                    onCancelled()
                } else {
                    onPostExecute(result)
                }

                status = AsyncTask.Status.FINISHED
            }
        )

        return this
    }

    fun getStatus(): AsyncTask.Status {
        return status
    }

    /**
     * Unlike [AsyncTask.cancel], this does not offer the option to attempt to interrupt the
     * background thread running [doInBackground]. Calling [cancel] will allow [doInBackground]
     * to finish, but will prevent [onPostExecute] from running ([onCancelled] will be run
     * instead).
     */
    fun cancel() {
        cancelled = true
    }

    fun isCancelled(): Boolean {
        return cancelled
    }

    protected fun publishProgress(vararg values: Progress) {
        scheduler.immediate(foreground = true) {
            onProgressUpdate(*values)
        }
    }
}

package org.odk.collect.async

interface TaskSpecScheduler {
    fun schedule(
        tag: String,
        spec: TaskSpec,
        inputData: Map<String, String>,
        networkConstraint: Scheduler.NetworkType? = null,
        repeatPeriod: Long? = null
    )
}

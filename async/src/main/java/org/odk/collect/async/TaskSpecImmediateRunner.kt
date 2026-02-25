package org.odk.collect.async

interface TaskSpecImmediateRunner {
    fun run(tag: String, taskSpec: TaskSpec, inputData: Map<String, String>, notificationInfo: NotificationInfo)
}

package org.odk.collect.async

interface TaskSpecRunner {
    fun run(tag: String, taskSpec: TaskSpec, inputData: Map<String, String>, notificationInfo: NotificationInfo)
}

package org.odk.collect.async

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class WorkManagerTaskSpecRunner(private val workManager: WorkManager) :
    TaskSpecRunner {
    override fun run(
        tag: String,
        taskSpec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        val workManagerInputData = Data.Builder()
            .putString(TaskSpecWorker.DATA_TASK_SPEC_CLASS, taskSpec.javaClass.name)
            .putBoolean(TaskSpecWorker.FOREGROUND, true)
            .putString(TaskSpecWorker.FOREGROUND_NOTIFICATION_CHANNEL, notificationInfo.channel)
            .putString(
                TaskSpecWorker.FOREGROUND_NOTIFICATION_CHANNEL_NAME,
                notificationInfo.channelName
            )
            .putInt(TaskSpecWorker.FOREGROUND_NOTIFICATION_TITLE, notificationInfo.title)
            .putInt(TaskSpecWorker.FOREGROUND_NOTIFICATION_ID, notificationInfo.id)
            .putAll(inputData)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
            .addTag(tag)
            .setInputData(workManagerInputData)
            .build()
        workManager.beginUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest).enqueue()
    }
}

package org.odk.collect.async.workmanager

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.odk.collect.async.NotificationInfo
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.TaskSpecRunner

class WorkManagerTaskSpecRunner(private val workManager: WorkManager) :
    TaskSpecRunner {
    override fun run(
        tag: String,
        taskSpec: TaskSpec,
        inputData: Map<String, String>,
        notificationInfo: NotificationInfo
    ) {
        val workManagerInputData = Data.Builder()
            .putString(TaskSpecWorker.Companion.DATA_TASK_SPEC_CLASS, taskSpec.javaClass.name)
            .putBoolean(TaskSpecWorker.Companion.FOREGROUND, true)
            .putString(TaskSpecWorker.Companion.FOREGROUND_NOTIFICATION_CHANNEL, notificationInfo.channel)
            .putString(
                TaskSpecWorker.Companion.FOREGROUND_NOTIFICATION_CHANNEL_NAME,
                notificationInfo.channelName
            )
            .putInt(TaskSpecWorker.Companion.FOREGROUND_NOTIFICATION_TITLE, notificationInfo.title)
            .putInt(TaskSpecWorker.Companion.FOREGROUND_NOTIFICATION_ID, notificationInfo.id)
            .putAll(inputData)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(TaskSpecWorker::class.java)
            .addTag(tag)
            .setInputData(workManagerInputData)
            .build()
        workManager.beginUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest).enqueue()
    }
}

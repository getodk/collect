package org.odk.collect.android.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.notifications.builders.FormUpdatesAvailableNotificationBuilder
import org.odk.collect.android.notifications.builders.FormUpdatesDownloadedNotificationBuilder
import org.odk.collect.android.notifications.builders.FormsSubmissionNotificationBuilder
import org.odk.collect.android.notifications.builders.FormsSyncFailedNotificationBuilder
import org.odk.collect.android.notifications.builders.FormsSyncStoppedNotificationBuilder
import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.androidshared.utils.UniqueIdGenerator
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.instances.Instance
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.strings.localization.getLocalizedString

class NotificationManagerNotifier(
    private val application: Application,
    private val settingsProvider: SettingsProvider,
    private val projectsRepository: ProjectsRepository,
    private val uniqueIdGenerator: UniqueIdGenerator
) : Notifier {
    private val notificationManager: NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String) {
        val notificationId = uniqueIdGenerator.getInt(FORM_UPDATE_NOTIFICATION_IDENTIFIER)

        val metaPrefs = settingsProvider.getMetaSettings()
        val updateId = updates
            .mapTo(HashSet()) { (_, _, formId, _, hash, _, _, manifest) -> formId + hash + manifest?.hash }
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION) != updateId) {
            notificationManager.notify(
                notificationId,
                FormUpdatesAvailableNotificationBuilder.build(
                    application,
                    getProjectName(projectId),
                    notificationId
                )
            )
            metaPrefs.save(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId)
        }
    }

    override fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String) {
        val notificationId = uniqueIdGenerator.getInt(FORM_UPDATE_NOTIFICATION_IDENTIFIER)

        notificationManager.notify(
            notificationId,
            FormUpdatesDownloadedNotificationBuilder.build(
                application,
                result,
                getProjectName(projectId),
                notificationId
            )
        )
    }

    override fun onSync(exception: FormSourceException?, projectId: String) {
        val notificationId = uniqueIdGenerator.getInt(FORM_SYNC_ERROR_NOTIFICATION_IDENTIFIER)

        if (exception == null) {
            notificationManager.cancel(notificationId)
        } else {
            notificationManager.notify(
                notificationId,
                FormsSyncFailedNotificationBuilder.build(
                    application,
                    exception,
                    getProjectName(projectId),
                    notificationId
                )
            )
        }
    }

    override fun onSyncStopped(projectId: String) {
        val notificationId = uniqueIdGenerator.getInt(FORM_SYNC_ERROR_NOTIFICATION_IDENTIFIER)

        notificationManager.notify(
            notificationId,
            FormsSyncStoppedNotificationBuilder.build(
                application,
                getProjectName(projectId),
                notificationId
            )
        )
    }

    override fun onSubmission(result: Map<Instance, FormUploadException?>, projectId: String) {
        val notificationId = uniqueIdGenerator.getInt(AUTO_SEND_RESULT_NOTIFICATION_IDENTIFIER)

        notificationManager.notify(
            notificationId,
            FormsSubmissionNotificationBuilder.build(
                application,
                result,
                getProjectName(projectId),
                notificationId
            )
        )
    }

    companion object {
        const val COLLECT_NOTIFICATION_CHANNEL = "collect_notification_channel"
        private val FORM_UPDATE_NOTIFICATION_IDENTIFIER = "form_update"
        private val FORM_SYNC_ERROR_NOTIFICATION_IDENTIFIER = "form_sync_error"
        private val AUTO_SEND_RESULT_NOTIFICATION_IDENTIFIER = "auto_send_result"
    }

    private fun getProjectName(projectId: String) = projectsRepository.get(projectId)?.name ?: ""

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    COLLECT_NOTIFICATION_CHANNEL,
                    application.getLocalizedString(org.odk.collect.strings.R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }
}

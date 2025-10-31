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
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.instances.Instance
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.strings.localization.getLocalizedString

class NotificationManagerNotifier(
    private val application: Application,
    private val settingsProvider: SettingsProvider,
    private val projectsRepository: ProjectsRepository
) : Notifier {
    private val notificationManager: NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String) {
        val metaPrefs = settingsProvider.getMetaSettings()
        val updateId = updates.mapTo(HashSet()) { (_, _, formId, _, hash, manifest, _) ->
            formId + hash + manifest?.hash
        }
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION) != updateId) {
            notificationManager.notify(
                FORM_UPDATE_NOTIFICATION_ID,
                FormUpdatesAvailableNotificationBuilder.build(
                    application,
                    getProjectName(projectId),
                    FORM_UPDATE_NOTIFICATION_ID
                )
            )
            metaPrefs.save(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId)
        }
    }

    override fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String) {
        notificationManager.notify(
            FORM_UPDATE_NOTIFICATION_ID,
            FormUpdatesDownloadedNotificationBuilder.build(
                application,
                result,
                getProjectName(projectId),
                FORM_UPDATE_NOTIFICATION_ID
            )
        )
    }

    override fun onSync(exception: FormSourceException?, projectId: String) {
        if (exception == null) {
            notificationManager.cancel(FORM_SYNC_NOTIFICATION_ID)
        } else {
            notificationManager.notify(
                FORM_SYNC_NOTIFICATION_ID,
                FormsSyncFailedNotificationBuilder.build(
                    application,
                    exception,
                    getProjectName(projectId),
                    FORM_SYNC_NOTIFICATION_ID
                )
            )
        }
    }

    override fun onSyncStopped(projectId: String) {
        notificationManager.notify(
            FORM_SYNC_NOTIFICATION_ID,
            FormsSyncStoppedNotificationBuilder.build(
                application,
                getProjectName(projectId),
                FORM_SYNC_NOTIFICATION_ID
            )
        )
    }

    override fun onSubmission(result: Map<Instance, FormUploadException?>, projectId: String) {
        notificationManager.notify(
            AUTO_SEND_RESULT_NOTIFICATION_ID,
            FormsSubmissionNotificationBuilder.build(
                application,
                result,
                getProjectName(projectId),
                AUTO_SEND_RESULT_NOTIFICATION_ID
            )
        )
    }

    companion object {
        const val COLLECT_NOTIFICATION_CHANNEL = "collect_notification_channel"
        const val FORM_UPDATE_NOTIFICATION_ID = 0
        const val FORM_SYNC_NOTIFICATION_ID = 1
        private const val AUTO_SEND_RESULT_NOTIFICATION_ID = 1328974928
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

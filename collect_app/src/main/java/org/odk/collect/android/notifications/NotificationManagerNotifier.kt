package org.odk.collect.android.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.forms.FormSourceException
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.strings.localization.getLocalizedString
import java.util.stream.Collectors

class NotificationManagerNotifier(
    private val application: Application,
    private val settingsProvider: SettingsProvider,
    private val projectsRepository: ProjectsRepository,
    private val formUpdatesDownloadedNotificationBuilder: FormUpdatesDownloadedNotificationBuilder,
    private val formsSyncFailedNotificationBuilder: FormsSyncFailedNotificationBuilder,
    private val formUpdatesAvailableNotificationBuilder: FormUpdatesAvailableNotificationBuilder,
    private val formsSubmissionNotificationBuilder: FormsSubmissionNotificationBuilder
) : Notifier {
    private val notificationManager: NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String) {
        val metaPrefs = settingsProvider.getMetaSettings()
        val updateId = updates.stream()
            .map { (_, _, formId, _, hash, _, _, manifest) -> formId + hash + manifest?.hash }
            .collect(Collectors.toSet())
        if (metaPrefs.getStringSet(MetaKeys.LAST_UPDATED_NOTIFICATION) == updateId) {
            return
        }
        notificationManager.notify(
            FORM_UPDATE_NOTIFICATION_ID,
            formUpdatesAvailableNotificationBuilder.build(
                getProjectName(projectId)
            )
        )
        metaPrefs.save(MetaKeys.LAST_UPDATED_NOTIFICATION, updateId)
    }

    override fun onUpdatesDownloaded(result: Map<ServerFormDetails, String>, projectId: String) {
        notificationManager.notify(
            FORM_UPDATE_NOTIFICATION_ID,
            formUpdatesDownloadedNotificationBuilder.build(
                result,
                getProjectName(projectId)
            )
        )
    }

    override fun onSync(exception: FormSourceException?, projectId: String) {
        if (exception == null) {
            notificationManager.cancel(FORM_SYNC_NOTIFICATION_ID)
        } else {
            notificationManager.notify(
                FORM_SYNC_NOTIFICATION_ID,
                formsSyncFailedNotificationBuilder.build(
                    exception,
                    getProjectName(projectId)
                )
            )
        }
    }

    override fun onSubmission(failure: Boolean, message: String) {
        notificationManager.notify(
            AUTO_SEND_RESULT_NOTIFICATION_ID,
            formsSubmissionNotificationBuilder.build(failure, message)
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
                    application.getLocalizedString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
    }
}

package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.FormDownloadException
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.notifications.NotificationUtils
import org.odk.collect.android.utilities.FormsDownloadResultInterpreter
import org.odk.collect.strings.localization.getLocalizedString

object FormUpdatesDownloadedNotificationBuilder {

    fun build(application: Application, result: Map<ServerFormDetails, FormDownloadException?>, projectName: String, notificationId: Int): Notification {
        val allFormsDownloadedSuccessfully = FormsDownloadResultInterpreter.allFormsDownloadedSuccessfully(result)

        val contentIntent = NotificationUtils.createOpenAppContentIntent(
            application,
            notificationId
        )

        val errorItems = FormsDownloadResultInterpreter.getFailures(result, application)
        val showDetailsIntent =
            NotificationUtils.createOpenErrorsActionIntent(application, errorItems, notificationId)

        val title =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(org.odk.collect.strings.R.string.forms_download_succeeded)
            } else {
                application.getLocalizedString(org.odk.collect.strings.R.string.forms_download_failed)
            }

        val message =
            if (allFormsDownloadedSuccessfully) {
                application.getLocalizedString(org.odk.collect.strings.R.string.all_downloads_succeeded)
            } else {
                application.getLocalizedString(
                    org.odk.collect.strings.R.string.some_downloads_failed,
                    FormsDownloadResultInterpreter.getNumberOfFailures(result),
                    result.size
                )
            }

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(title)
            setContentText(message)
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)

            if (!allFormsDownloadedSuccessfully) {
                addAction(
                    R.drawable.ic_outline_info_small,
                    application.getLocalizedString(org.odk.collect.strings.R.string.show_details),
                    showDetailsIntent
                )
            }
        }.build()
    }
}

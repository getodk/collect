package org.odk.collect.android.notifications

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.activities.NotificationActivity
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION
import org.odk.collect.android.utilities.IconUtils
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.strings.localization.getLocalizedString

class FormUpdatesDownloadedNotificationBuilder(private val application: Application) {

    fun build(result: Map<ServerFormDetails, String>): Notification {
        val intent = Intent(application, NotificationActivity::class.java)
        intent.putExtra(
            NotificationActivity.NOTIFICATION_TITLE,
            TranslationHandler.getString(application, R.string.download_forms_result)
        )
        intent.putExtra(
            NotificationActivity.NOTIFICATION_MESSAGE,
            FormDownloadListActivity.getDownloadResultMessage(result)
        )
        val contentIntent = PendingIntent.getActivity(
            application,
            FORMS_DOWNLOADED_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val content =
            application.getLocalizedString(if (allFormsDownloadedSuccessfully(result)) R.string.success else R.string.failures)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(
                TranslationHandler.getString(
                    application,
                    R.string.odk_auto_download_notification_title
                )
            )
            setContentText(content)
            setSmallIcon(IconUtils.getNotificationAppIcon())
            setAutoCancel(true)
        }.build()
    }

    private fun allFormsDownloadedSuccessfully(result: Map<ServerFormDetails, String>) =
        result.values.all {
            it == application.getLocalizedString(R.string.success)
        }
}

package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.NotificationActivity
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.android.utilities.IconUtils
import org.odk.collect.android.utilities.TranslationHandler
import org.odk.collect.strings.localization.getLocalizedString

class FormsSubmissionNotificationBuilder(private val application: Application) {

    fun build(submissionFailed: Boolean, message: String, projectName: String): Notification {
        val notifyIntent = Intent(application, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                NotificationActivity.NOTIFICATION_TITLE,
                application.getLocalizedString(R.string.upload_results)
            )
            putExtra(NotificationActivity.NOTIFICATION_MESSAGE, message.trim { it <= ' ' })
        }

        val pendingNotify = PendingIntent.getActivity(
            application, RequestCodes.FORMS_UPLOADED_NOTIFICATION,
            notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val content =
            if (submissionFailed) application.getLocalizedString(R.string.failures)
            else TranslationHandler.getString(application, R.string.success)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(pendingNotify)
            setContentTitle(TranslationHandler.getString(application, R.string.odk_auto_note))
            setContentText(content)
            setSubText(projectName)
            setSmallIcon(IconUtils.getNotificationAppIcon())
            setAutoCancel(true)
        }.build()
    }
}

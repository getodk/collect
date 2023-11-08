package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.FormSourceExceptionMapper
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.notifications.NotificationUtils
import org.odk.collect.android.notifications.NotificationUtils.createOpenErrorsActionIntent
import org.odk.collect.errors.ErrorItem
import org.odk.collect.forms.FormSourceException
import org.odk.collect.strings.localization.getLocalizedString

object FormsSyncFailedNotificationBuilder {

    fun build(application: Application, exception: FormSourceException, projectName: String, notificationId: Int): Notification {
        val contentIntent = NotificationUtils.createOpenAppContentIntent(application, notificationId)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(org.odk.collect.strings.R.string.form_update_error))
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
            addAction(
                R.drawable.ic_outline_info_small,
                application.getLocalizedString(org.odk.collect.strings.R.string.show_details),
                getShowDetailsPendingIntent(application, projectName, exception, notificationId)
            )
        }.build()
    }

    private fun getShowDetailsPendingIntent(
        application: Application,
        projectName: String,
        exception: FormSourceException,
        notificationId: Int
    ): PendingIntent {
        val errorItem = ErrorItem(
            application.getLocalizedString(org.odk.collect.strings.R.string.form_update_error),
            projectName,
            FormSourceExceptionMapper(application).getMessage(exception)
        )

        return createOpenErrorsActionIntent(application, listOf(errorItem), notificationId)
    }
}

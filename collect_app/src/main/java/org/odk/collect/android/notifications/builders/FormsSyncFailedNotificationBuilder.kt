package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.formmanagement.FormSourceExceptionMapper
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.forms.FormSourceException
import org.odk.collect.strings.localization.getLocalizedString

object FormsSyncFailedNotificationBuilder {

    fun build(application: Application, exception: FormSourceException, projectName: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            application,
            NotificationManagerNotifier.FORM_SYNC_NOTIFICATION_ID,
            Intent(application, MainMenuActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(org.odk.collect.strings.R.string.form_update_error))
            setSubText(projectName)
            setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(FormSourceExceptionMapper(application).getMessage(exception))
            )
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}

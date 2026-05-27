package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.formlists.blankformlist.BlankFormListActivity
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.strings.localization.getLocalizedString

object FormsSyncStoppedNotificationBuilder {

    fun build(application: Application, projectName: String, notificationId: Int): Notification {
        val contentIntent = PendingIntent.getActivity(
            application,
            notificationId,
            Intent(application, BlankFormListActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(org.odk.collect.strings.R.string.form_update_error))
            setContentText(application.getLocalizedString(org.odk.collect.strings.R.string.form_update_error_massage))
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}

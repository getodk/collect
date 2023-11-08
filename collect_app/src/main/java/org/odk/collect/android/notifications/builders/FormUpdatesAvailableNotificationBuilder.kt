package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.notifications.NotificationUtils
import org.odk.collect.android.utilities.ApplicationConstants.RequestCodes
import org.odk.collect.strings.localization.getLocalizedString

object FormUpdatesAvailableNotificationBuilder {

    @JvmStatic
    fun build(application: Application, projectName: String): Notification {
        val contentIntent = NotificationUtils.createOpenAppContentIntent(
            application,
            RequestCodes.FORM_UPDATES_AVAILABLE_NOTIFICATION
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(org.odk.collect.strings.R.string.form_updates_available))
            setContentText(null)
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
        }.build()
    }
}

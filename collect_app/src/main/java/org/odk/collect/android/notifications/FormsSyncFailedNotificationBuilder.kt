package org.odk.collect.android.notifications

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.FillBlankFormActivity
import org.odk.collect.android.formmanagement.FormSourceExceptionMapper
import org.odk.collect.android.utilities.IconUtils
import org.odk.collect.forms.FormSourceException
import org.odk.collect.strings.localization.getLocalizedString

class FormsSyncFailedNotificationBuilder(private val application: Application) {

    fun build(exception: FormSourceException): Notification {
        val contentIntent = PendingIntent.getActivity(
            application,
            NotificationManagerNotifier.FORM_SYNC_NOTIFICATION_ID,
            Intent(application, FillBlankFormActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(application.getLocalizedString(R.string.form_update_error))
            setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(FormSourceExceptionMapper(application).getMessage(exception))
            )
            setSmallIcon(IconUtils.getNotificationAppIcon())
            setAutoCancel(true)
        }.build()
    }
}

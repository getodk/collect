package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.formmanagement.FormSourceExceptionMapper
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.errors.ErrorActivity
import org.odk.collect.errors.ErrorItem
import org.odk.collect.forms.FormSourceException
import org.odk.collect.strings.localization.getLocalizedString
import java.io.Serializable

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
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)
            addAction(
                R.drawable.ic_outline_info_small,
                application.getLocalizedString(org.odk.collect.strings.R.string.show_details),
                getShowDetailsPendingIntent(application, projectName, exception)
            )
        }.build()
    }

    private fun getShowDetailsPendingIntent(application: Application, projectName: String, exception: FormSourceException): PendingIntent {
        val errorItem = ErrorItem(
            application.getLocalizedString(org.odk.collect.strings.R.string.form_update_error),
            projectName,
            FormSourceExceptionMapper(application).getMessage(exception)
        )
        val showDetailsIntent = Intent(application, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_ERRORS, listOf(errorItem) as Serializable)
        }

        return PendingIntent.getActivity(
            application,
            ApplicationConstants.RequestCodes.FORMS_UPLOADED_NOTIFICATION,
            showDetailsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

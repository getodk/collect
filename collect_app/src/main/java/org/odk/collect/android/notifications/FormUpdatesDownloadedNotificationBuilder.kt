package org.odk.collect.android.notifications

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.activities.FillBlankFormActivity
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.android.utilities.FormsDownloadResultInterpreter
import org.odk.collect.android.utilities.IconUtils
import org.odk.collect.errors.ErrorActivity
import org.odk.collect.strings.localization.getLocalizedString
import java.io.Serializable

class FormUpdatesDownloadedNotificationBuilder(
    private val application: Application,
    private val formsDownloadResultInterpreter: FormsDownloadResultInterpreter
) {

    fun build(result: Map<ServerFormDetails, String>): Notification {
        val allFormsDownloadedSuccessfully = formsDownloadResultInterpreter.allFormsDownloadedSuccessfully(result)

        val intent = if (allFormsDownloadedSuccessfully) {
            Intent(application, FillBlankFormActivity::class.java)
        } else {
            Intent(application, ErrorActivity::class.java).apply {
                putExtra(ErrorActivity.EXTRA_ERRORS, formsDownloadResultInterpreter.getFailures(result) as Serializable)
            }
        }

        val contentIntent = PendingIntent.getActivity(
            application,
            ApplicationConstants.RequestCodes.FORMS_DOWNLOADED_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title =
            if (allFormsDownloadedSuccessfully) application.getLocalizedString(R.string.forms_download_succeeded)
            else application.getLocalizedString(R.string.forms_download_failed)

        val message =
            if (allFormsDownloadedSuccessfully) application.getLocalizedString(R.string.all_downloads_succeeded)
            else application.getLocalizedString(
                R.string.some_downloads_failed,
                formsDownloadResultInterpreter.getNumberOfFailures(result),
                result.size
            )

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(contentIntent)
            setContentTitle(title)
            setContentText(message)
            setSmallIcon(IconUtils.getNotificationAppIcon())
            setAutoCancel(true)
        }.build()
    }
}

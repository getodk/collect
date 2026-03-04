package org.odk.collect.android.notifications.builders

import android.app.Application
import android.app.Notification
import androidx.core.app.NotificationCompat
import org.odk.collect.android.R
import org.odk.collect.android.instancemanagement.InstanceUploadResult
import org.odk.collect.android.notifications.NotificationManagerNotifier
import org.odk.collect.android.notifications.NotificationUtils
import org.odk.collect.android.utilities.FormsUploadResultInterpreter
import org.odk.collect.strings.localization.getLocalizedString

object FormsSubmissionNotificationBuilder {

    fun build(
        application: Application,
        uploadResults: List<InstanceUploadResult>,
        projectName: String,
        notificationId: Int
    ): Notification {
        val allFormsUploadedSuccessfully = FormsUploadResultInterpreter.allFormsUploadedSuccessfully(uploadResults)

        return NotificationCompat.Builder(
            application,
            NotificationManagerNotifier.COLLECT_NOTIFICATION_CHANNEL
        ).apply {
            setContentIntent(
                NotificationUtils.createOpenAppContentIntent(
                    application,
                    notificationId
                )
            )
            setContentTitle(getTitle(application, allFormsUploadedSuccessfully))
            setContentText(getMessage(application, allFormsUploadedSuccessfully, uploadResults))
            setSubText(projectName)
            setSmallIcon(org.odk.collect.icons.R.drawable.ic_notification_small)
            setAutoCancel(true)

            if (!allFormsUploadedSuccessfully) {
                val errorItems = FormsUploadResultInterpreter.getFailures(uploadResults, application)

                addAction(
                    R.drawable.ic_outline_info_small,
                    application.getLocalizedString(org.odk.collect.strings.R.string.show_details),
                    NotificationUtils.createOpenErrorsActionIntent(application, errorItems, notificationId)
                )
            }
        }.build()
    }

    private fun getTitle(application: Application, allFormsUploadedSuccessfully: Boolean): String {
        return if (allFormsUploadedSuccessfully) {
            application.getLocalizedString(org.odk.collect.strings.R.string.forms_upload_succeeded)
        } else {
            application.getLocalizedString(org.odk.collect.strings.R.string.forms_upload_failed)
        }
    }

    private fun getMessage(application: Application, allFormsUploadedSuccessfully: Boolean, uploadResults: List<InstanceUploadResult>): String {
        return if (allFormsUploadedSuccessfully) {
            application.getLocalizedString(org.odk.collect.strings.R.string.all_uploads_succeeded)
        } else {
            application.getLocalizedString(
                org.odk.collect.strings.R.string.some_uploads_failed,
                FormsUploadResultInterpreter.getNumberOfFailures(uploadResults),
                uploadResults.size
            )
        }
    }
}

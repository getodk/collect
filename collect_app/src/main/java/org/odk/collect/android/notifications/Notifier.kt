package org.odk.collect.android.notifications

import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.instancemanagement.InstanceUploadResult
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.instances.Instance

interface Notifier {
    fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String)
    fun onUpdatesDownloaded(result: Map<ServerFormDetails, FormDownloadException?>, projectId: String)
    fun onSync(exception: FormSourceException?, projectId: String)
    fun onSyncStopped(projectId: String)
    fun onSubmission(uploadResults: List<InstanceUploadResult>, projectId: String)
}

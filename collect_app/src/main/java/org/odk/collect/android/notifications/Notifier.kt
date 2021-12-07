package org.odk.collect.android.notifications

import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.forms.FormSourceException

interface Notifier {
    fun onUpdatesAvailable(updates: List<ServerFormDetails>, projectId: String)
    fun onUpdatesDownloaded(result: Map<ServerFormDetails, String>, projectId: String)
    fun onSync(exception: FormSourceException?, projectId: String)
    fun onSubmission(failure: Boolean, message: String)
}

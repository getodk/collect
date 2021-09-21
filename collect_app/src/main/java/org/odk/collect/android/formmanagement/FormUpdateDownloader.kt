package org.odk.collect.android.formmanagement

import org.odk.collect.forms.FormSourceException
import org.odk.collect.shared.locks.ChangeLock

class FormUpdateDownloader {

    fun downloadUpdates(
        updatedForms: List<ServerFormDetails>,
        changeLock: ChangeLock,
        formDownloader: FormDownloader,
        successMessage: String,
        failureMessage: String
    ): Map<ServerFormDetails, String> {
        val results = mutableMapOf<ServerFormDetails, String>()

        changeLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                for (serverFormDetails in updatedForms) {
                    try {
                        formDownloader.downloadForm(serverFormDetails, null, null)
                        results[serverFormDetails] = successMessage
                    } catch (e: FormSourceException.DownloadingInterrupted) {
                        break
                    } catch (e: FormSourceException) {
                        results[serverFormDetails] = failureMessage
                    }
                }
            }
        }

        return results
    }
}

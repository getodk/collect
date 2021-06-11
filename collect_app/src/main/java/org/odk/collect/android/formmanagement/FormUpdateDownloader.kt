package org.odk.collect.android.formmanagement

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
                    } catch (e: FormDownloadException) {
                        results[serverFormDetails] = failureMessage
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }
        }

        return results
    }
}

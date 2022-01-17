package org.odk.collect.android.formmanagement

import org.odk.collect.shared.locks.ChangeLock

class FormUpdateDownloader {

    fun downloadUpdates(
        updatedForms: List<ServerFormDetails>,
        changeLock: ChangeLock,
        formDownloader: FormDownloader,
        successMessage: String,
        failureMessage: String
    ): Map<ServerFormDetails, FormDownloadException?> {
        val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()

        changeLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                for (serverFormDetails in updatedForms) {
                    try {
                        formDownloader.downloadForm(serverFormDetails, null, null)
                        results[serverFormDetails] = null
                    } catch (e: FormDownloadException.DownloadingInterrupted) {
                        break
                    } catch (e: FormDownloadException) {
                        results[serverFormDetails] = e
                    }
                }
            }
        }

        return results
    }
}

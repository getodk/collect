package org.odk.collect.android.formmanagement

import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.FormDownloader
import org.odk.collect.shared.locks.ChangeLock

object ServerFormUseCases {

    fun downloadForms(
        forms: List<ServerFormDetails>,
        changeLock: ChangeLock,
        formDownloader: FormDownloader,
        progressReporter: ((Int, Int) -> Unit)? = null,
        isCancelled: (() -> Boolean)? = null,
    ): Map<ServerFormDetails, FormDownloadException?> {
        val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()
        changeLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                for (index in forms.indices) {
                    val form = forms[index]

                    try {
                        formDownloader.downloadForm(
                            form,
                            object : FormDownloader.ProgressReporter {
                                override fun onDownloadingMediaFile(count: Int) {
                                    progressReporter?.invoke(index, count)
                                }
                            },
                            { isCancelled?.invoke() ?: false }
                        )

                        results[form] = null
                    } catch (e: FormDownloadException.DownloadingInterrupted) {
                        break
                    } catch (e: FormDownloadException) {
                        results[form] = e
                    }
                }
            }
        }

        return results
    }
}

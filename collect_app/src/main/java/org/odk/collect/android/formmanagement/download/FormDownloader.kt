package org.odk.collect.android.formmanagement.download

import org.odk.collect.android.formmanagement.FormFileDownload
import org.odk.collect.android.formmanagement.MediaFilesDownload
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.shared.result.Result
import org.odk.collect.shared.result.chain
import org.odk.collect.shared.result.onError
import java.util.function.Supplier

interface FormDownloader {

    @Throws(FormDownloadException::class)
    fun downloadAndProcessForm(
        form: ServerFormDetails,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean>?
    ) {
        val formDownload = downloadForm(form, progressReporter, isCancelled)
            .onError { throw it }

        formDownload.chain { (formFileDownload, mediaFilesDownload) ->
            processForm(formFileDownload, mediaFilesDownload)
        }.onError { throw it }
    }

    fun downloadForm(
        form: ServerFormDetails,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean>?,
    ): Result<Pair<FormFileDownload, MediaFilesDownload>, FormDownloadException>

    fun processForm(
        formFileDownload: FormFileDownload,
        mediaFilesDownload: MediaFilesDownload,
    ): Result<Any, FormDownloadException>

    interface ProgressReporter {
        fun onDownloadingMediaFile(count: Int)
    }
}

fun FormDownloader.downloadForms(
    forms: List<ServerFormDetails>,
    progressReporter: ((Int, Int) -> Unit)? = null,
    isCancelled: (() -> Boolean)? = null,
): Map<ServerFormDetails, FormDownloadException?> {
    val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()
    for (index in forms.indices) {
        val form = forms[index]

        try {
            downloadAndProcessForm(
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

    return results
}

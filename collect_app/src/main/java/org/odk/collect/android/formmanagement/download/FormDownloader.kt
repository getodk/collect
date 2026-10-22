package org.odk.collect.android.formmanagement.download

import org.odk.collect.android.formmanagement.FormDownload
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

        formDownload.chain {
            processForm(it)
        }.onError { throw it }
    }

    fun downloadForm(
        form: ServerFormDetails,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean>?,
    ): Result<FormDownload, FormDownloadException>

    fun processForm(formDownload: FormDownload): Result<Any, FormDownloadException>

    interface ProgressReporter {
        fun onDownloadingMediaFile(count: Int)
    }
}

fun FormDownloader.downloadAndProcessForms(
    forms: List<ServerFormDetails>,
    progressReporter: ((Int, Int) -> Unit)? = null,
    isCancelled: (() -> Boolean)? = null,
): Map<ServerFormDetails, FormDownloadException?> {
    val downloadResults = downloadForms(forms, progressReporter, isCancelled)
    val results = mutableMapOf<ServerFormDetails, FormDownloadException?>()
    forms.zip(downloadResults).forEach { (formDetails, result) ->
        when (result) {
            is Result.Success -> {
                processForm(result.value)
                results[formDetails] = null
            }

            is Result.Error -> {
                results[formDetails] = result.value
            }
        }
    }

    return results
}

fun FormDownloader.downloadForms(
    forms: List<ServerFormDetails>,
    progressReporter: ((Int, Int) -> Unit)? = null,
    isCancelled: (() -> Boolean)? = null
): List<Result<FormDownload, FormDownloadException>> {
    return forms.mapIndexed { index, details ->
        downloadForm(
            details,
            object : FormDownloader.ProgressReporter {
                override fun onDownloadingMediaFile(count: Int) {
                    progressReporter?.invoke(index, count)
                }
            },
            { isCancelled?.invoke() ?: false }
        )
    }
}

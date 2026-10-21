package org.odk.collect.android.formmanagement.download

import org.odk.collect.android.formmanagement.ServerFormDetails
import java.util.function.Supplier

interface FormDownloader {

    @Throws(FormDownloadException::class)
    fun downloadForm(
        form: ServerFormDetails,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean>?
    )

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
            downloadForm(
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

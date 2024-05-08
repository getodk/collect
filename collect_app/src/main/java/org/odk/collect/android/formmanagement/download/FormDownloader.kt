package org.odk.collect.android.formmanagement.download

import org.odk.collect.android.formmanagement.ServerFormDetails
import java.util.function.Supplier

interface FormDownloader {

    @Throws(FormDownloadException::class)
    fun downloadForm(
        form: ServerFormDetails?,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean?>?
    )

    interface ProgressReporter {
        fun onDownloadingMediaFile(count: Int)
    }
}

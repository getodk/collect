package org.odk.collect.android.formmanagement

import org.odk.collect.forms.FormSourceException
import java.util.function.Supplier

interface FormDownloader {

    @Throws(FormSourceException::class)
    fun downloadForm(
        form: ServerFormDetails?,
        progressReporter: ProgressReporter?,
        isCancelled: Supplier<Boolean?>?
    )

    interface ProgressReporter {
        fun onDownloadingMediaFile(count: Int)
    }
}

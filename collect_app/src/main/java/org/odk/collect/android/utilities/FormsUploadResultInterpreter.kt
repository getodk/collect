package org.odk.collect.android.utilities

import org.odk.collect.android.upload.FormUploadException
import org.odk.collect.forms.instances.Instance

object FormsUploadResultInterpreter {

    fun getNumberOfFailures(result: Map<Instance, FormUploadException?>) = result.count {
        it.value != null
    }

    fun allFormsUploadedSuccessfully(result: Map<Instance, FormUploadException?>) = result.values.all {
        it == null
    }
}

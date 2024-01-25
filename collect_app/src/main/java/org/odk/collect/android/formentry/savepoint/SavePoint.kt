package org.odk.collect.android.formentry.savepoint

import org.odk.collect.forms.Form
import java.io.File

data class SavePoint(
    val file: File,
    val form: Form
)

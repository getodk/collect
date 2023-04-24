package org.odk.collect.android.formentry

sealed class FormError {
    abstract val message: String

    data class NonFatal(override val message: String) : FormError()

    data class Fatal(override val message: String) : FormError()
}

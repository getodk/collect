package org.odk.collect.android.formentry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
sealed class FormError : Parcelable {
    abstract val message: String

    data class NonFatal(override val message: String) : FormError()

    data class Fatal(override val message: String) : FormError()
}

package org.odk.collect.androidshared.system

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat

object BundleExt {

    inline fun <reified T : Parcelable> Bundle.getParcelableCompat(name: String): T? {
        return BundleCompat.getParcelable(this, name, T::class.java)
    }
}
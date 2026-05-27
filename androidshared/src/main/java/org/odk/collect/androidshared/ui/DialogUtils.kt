package org.odk.collect.androidshared.ui

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {
    @JvmStatic
    fun show(
        context: Context,
        @StringRes titleRes: Int,
        @StringRes messageRes: Int,
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(org.odk.collect.strings.R.string.ok, null)
            .show()
    }
}

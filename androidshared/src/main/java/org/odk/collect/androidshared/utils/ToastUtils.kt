package org.odk.collect.androidshared.utils

import android.app.Application
import android.content.Context
import android.widget.Toast
import org.odk.collect.strings.getLocalizedString

/**
 * Convenience wrapper around Android's [Toast] API.
 */
object ToastUtils {

    private lateinit var lastToast: Toast

    @JvmStatic
    fun showShortToast(context: Context, message: String) {
        showToast(context.applicationContext as Application, message)
    }

    @JvmStatic
    fun showShortToast(context: Context, messageResource: Int) {
        showToast(
            context.applicationContext as Application,
            context.getLocalizedString(messageResource)
        )
    }

    @JvmStatic
    fun showLongToast(context: Context, message: String) {
        showToast(context.applicationContext as Application, message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showLongToast(context: Context, messageResource: Int) {
        showToast(
            context.applicationContext as Application,
            context.getLocalizedString(messageResource),
            Toast.LENGTH_LONG
        )
    }

    private fun showToast(
        context: Application,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        hideLastToast()
        lastToast = Toast.makeText(context, message, duration)
        lastToast.show()
    }

    private fun hideLastToast() {
        if (ToastUtils::lastToast.isInitialized) {
            lastToast.cancel()
        }
    }
}

package org.odk.collect.androidshared.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.R
import org.odk.collect.strings.localization.getLocalizedString

/**
 * Convenience wrapper around Android's [Toast] API.
 */
object ToastUtils {

    @JvmStatic
    var recordToasts = false
    private var recordedToasts = mutableListOf<String>()

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

    @JvmStatic
    @Deprecated("Toast position cannot be customized on API 30 and above. A dialog is shown instead for this API levels.")
    fun showShortToastInMiddle(activity: Activity, message: String) {
        showToastInMiddle(activity, message)
    }

    @JvmStatic
    fun popRecordedToasts(): List<String> {
        val copy = recordedToasts.toList()
        recordedToasts.clear()

        return copy
    }

    private fun showToast(
        context: Application,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        hideLastToast()
        lastToast = Toast.makeText(context, message, duration)
        lastToast.show()

        if (recordToasts) {
            recordedToasts.add(message)
        }
    }

    private fun showToastInMiddle(
        activity: Activity,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        if (Build.VERSION.SDK_INT < 30) {
            hideLastToast()
            lastToast = Toast.makeText(activity.applicationContext, message, duration)
            try {
                val group = lastToast.view as ViewGroup?
                val messageTextView = group!!.getChildAt(0) as TextView
                messageTextView.textSize = 21f
                messageTextView.gravity = Gravity.CENTER
            } catch (ignored: Exception) {
                // ignored
            }
            lastToast.setGravity(Gravity.CENTER, 0, 0)
            lastToast.show()

            if (recordToasts) {
                recordedToasts.add(message)
            }
        } else {
            MaterialAlertDialogBuilder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
        }
    }

    private fun hideLastToast() {
        if (ToastUtils::lastToast.isInitialized) {
            lastToast.cancel()
        }
    }
}

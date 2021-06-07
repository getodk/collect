package org.odk.collect.android.utilities

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import org.odk.collect.android.application.Collect

object ToastUtils {
    lateinit var toast: Toast

    @JvmStatic
    fun showShortToast(message: String) {
        showToast(message)
    }

    @JvmStatic
    fun showShortToast(messageResource: Int) {
        showToast(TranslationHandler.getString(Collect.getInstance(), messageResource))
    }

    @JvmStatic
    fun showLongToast(message: String) {
        showToast(message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showLongToast(messageResource: Int) {
        showToast(TranslationHandler.getString(Collect.getInstance(), messageResource), Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showShortToastInMiddle(message: String) {
        showToastInMiddle(message)
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        hideOldToast()
        toast = Toast.makeText(Collect.getInstance(), message, duration)
        toast.show()
    }

    private fun showToastInMiddle(message: String, duration: Int = Toast.LENGTH_SHORT) {
        hideOldToast()
        toast = Toast.makeText(Collect.getInstance(), message, duration)
        try {
            val group = toast.view as ViewGroup?
            val messageTextView = group!!.getChildAt(0) as TextView
            messageTextView.textSize = 21f
            messageTextView.gravity = Gravity.CENTER
        } catch (ignored: Exception) {
            // ignored
        }
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun hideOldToast() {
        if (::toast.isInitialized) {
            toast.cancel()
        }
    }
}

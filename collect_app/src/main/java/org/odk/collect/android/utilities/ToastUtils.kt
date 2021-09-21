package org.odk.collect.android.utilities

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

object ToastUtils {

    private lateinit var lastToast: Toast

    @JvmStatic
    fun showShortToast(context: Context, message: String) {
        showToast(context, message)
    }

    @JvmStatic
    fun showShortToast(context: Context, messageResource: Int) {
        showToast(context, TranslationHandler.getString(context, messageResource))
    }

    @JvmStatic
    fun showLongToast(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showLongToast(context: Context, messageResource: Int) {
        showToast(
            context,
            TranslationHandler.getString(context, messageResource),
            Toast.LENGTH_LONG
        )
    }

    @JvmStatic
    fun showShortToastInMiddle(context: Context, message: String) {
        showToastInMiddle(context, message)
    }

    private fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        hideLastToast()
        lastToast = Toast.makeText(context, message, duration)
        lastToast.show()
    }

    private fun showToastInMiddle(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        hideLastToast()
        lastToast = Toast.makeText(context, message, duration)
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
    }

    private fun hideLastToast() {
        if (::lastToast.isInitialized) {
            lastToast.cancel()
        }
    }
}

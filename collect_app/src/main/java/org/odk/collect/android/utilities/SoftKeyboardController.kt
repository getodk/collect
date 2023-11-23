package org.odk.collect.android.utilities

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager

object SoftKeyboardController {
    fun showSoftKeyboard(view: View) {
        // Subtle delay in displaying the keyboard is necessary for the keyboard to be displayed at all,
        // for example, when returning from the hierarchy view.
        Handler(Looper.getMainLooper()).postDelayed({
            if (view.requestFocus()) {
                val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(view, 0)
            }
        }, 100)
    }

    fun hideSoftKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

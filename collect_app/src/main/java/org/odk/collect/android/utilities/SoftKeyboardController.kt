package org.odk.collect.android.utilities

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object SoftKeyboardController {
    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, 0)
        }
    }

    fun hideSoftKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

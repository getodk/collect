/*

Copyright 2018 Shobhit
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.odk.collect.androidshared.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import org.odk.collect.androidshared.R

/**
 * Convenience wrapper around Android's [Snackbar] API.
 */
object SnackbarUtils {
    @JvmStatic
    @JvmOverloads
    fun showShortSnackbar(
        parentView: View,
        message: String,
        anchorView: View? = null,
        action: Action? = null,
        displayDismissButton: Boolean = false
    ) {
        return showSnackbar(parentView, message, 3500, anchorView, action, displayDismissButton)
    }

    @JvmStatic
    @JvmOverloads
    fun showLongSnackbar(
        parentView: View,
        message: String,
        anchorView: View? = null,
        action: Action? = null,
        displayDismissButton: Boolean = false
    ) {
        return showSnackbar(parentView, message, 5500, anchorView, action, displayDismissButton)
    }

    /**
     * Displays snackbar with {@param message} and multi-line message enabled.
     *
     * @param parentView            The view to find a parent from.
     * @param anchorView            The view this snackbar should be anchored above.
     * @param message               The text to show. Can be formatted text.
     * @param displayDismissButton  True if the dismiss button should be displayed, false otherwise.
     */
    private fun showSnackbar(
        parentView: View,
        message: String,
        duration: Int,
        anchorView: View?,
        action: Action? = null,
        displayDismissButton: Boolean
    ) {
        if (message.isBlank()) {
            return
        }

        Snackbar.make(parentView, message.trim(), duration).apply {
            val textView = view.findViewById<TextView>(R.id.snackbar_text)
            textView.isSingleLine = false

            if (anchorView?.visibility != View.GONE) {
                this.anchorView = anchorView
            }

            if (displayDismissButton) {
                view.findViewById<Button>(R.id.snackbar_action).let {
                    val dismissButton = ImageView(view.context).apply {
                        setImageResource(R.drawable.ic_close_24)
                        setOnClickListener {
                            dismiss()
                        }
                        contentDescription = context.getString(R.string.close_snackbar)
                    }

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    params.setMargins(16, 0, 0, 0)

                    (it.parent as ViewGroup).addView(dismissButton, params)
                }
            }

            if (action != null) {
                setAction(action.text) {
                    action.listener.invoke()
                    dismiss()
                }
            }
        }.show()
    }

    data class Action(
        val text: String,
        val listener: () -> Unit
    )
}

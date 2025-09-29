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
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.odk.collect.androidshared.data.Consumable

/**
 * Convenience wrapper around Android's [Snackbar] API.
 */
object SnackbarUtils {

    const val DURATION_SHORT = 3500
    const val DURATION_LONG = 5500

    private var lastSnackbar: Snackbar? = null

    /**
     * Displays snackbar with {@param message} and multi-line message enabled.
     *
     * @param parentView            The view to find a parent from.
     * @param anchorView            The view this snackbar should be anchored above.
     * @param message               The text to show. Can be formatted text.
     * @param displayDismissButton  True if the dismiss button should be displayed, false otherwise.
     */
    @JvmStatic
    @JvmOverloads
    fun showSnackbar(
        parentView: View,
        message: String,
        duration: Int,
        anchorView: View? = null,
        action: Action? = null,
        displayDismissButton: Boolean = false,
        onDismiss: () -> Unit = {}
    ) {
        if (message.isBlank()) {
            return
        }

        lastSnackbar?.dismiss()
        lastSnackbar = Snackbar.make(parentView, message.trim(), duration).apply {
            val textView =
                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.isSingleLine = false

            if (anchorView?.visibility != View.GONE) {
                this.anchorView = anchorView
            }

            if (displayDismissButton) {
                view.findViewById<Button>(com.google.android.material.R.id.snackbar_action).let {
                    val dismissButton = ImageView(view.context).apply {
                        setImageResource(org.odk.collect.androidshared.R.drawable.ic_close_24)
                        setOnClickListener {
                            dismiss()
                        }
                        contentDescription =
                            context.getString(org.odk.collect.strings.R.string.close_snackbar)
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
                    action.beforeDismiss.invoke()
                    dismiss()
                }
            }
        }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                onDismiss()
                lastSnackbar = null
            }
        })
        lastSnackbar?.show()
    }

    data class SnackbarDetails @JvmOverloads constructor(
        val text: String,
        val action: Action? = null
    )

    data class Action(val text: String, val beforeDismiss: () -> Unit = {})

    abstract class SnackbarPresenterObserver<T : Any?>(private val parentView: View) :
        Observer<Consumable<T>?> {

        abstract fun getSnackbarDetails(value: T): SnackbarDetails

        override fun onChanged(consumable: Consumable<T>?) {
            if (consumable != null && !consumable.isConsumed()) {
                showSnackbar(
                    parentView,
                    getSnackbarDetails(consumable.value).text,
                    DURATION_LONG,
                    action = getSnackbarDetails(consumable.value).action
                )
                consumable.consume()
            }
        }
    }
}

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
import android.widget.TextView
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar

/**
 * Convenience wrapper around Android's [Snackbar] API.
 */
object SnackbarUtils {
    @JvmStatic
    @JvmOverloads
    fun showShortSnackbar(parentView: View, message: String, anchorView: View? = null) {
        showSnackbar(parentView, message, 3500, anchorView)
    }

    @JvmStatic
    @JvmOverloads
    fun showLongSnackbar(parentView: View, message: String, anchorView: View? = null) {
        showSnackbar(parentView, message, 5500, anchorView)
    }

    /**
     * Displays snackbar with {@param message} and multi-line message enabled.
     *
     * @param parentView    The view to find a parent from.
     * @param anchorView    The view this snackbar should be anchored above.
     * @param message       The text to show.  Can be formatted text.
     */
    private fun showSnackbar(parentView: View, message: String, duration: Int, anchorView: View? = null) {
        Snackbar.make(parentView, message.trim(), duration).apply {
            val textView = this.view.findViewById<TextView>(R.id.snackbar_text)
            textView.isSingleLine = false

            this.anchorView = anchorView
        }.show()
    }
}

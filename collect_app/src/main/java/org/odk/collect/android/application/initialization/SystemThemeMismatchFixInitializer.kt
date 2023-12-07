package org.odk.collect.android.application.initialization

import android.content.Context
import android.os.Build
import android.webkit.WebView

/**
 * When the app's theme doesn't match the system, the first [WebView] created will cause the host
 * Activity to be recreated with incorrect resource loading (the system ones instead of the custom
 * set dark/light resources). This doesn't happen for subsequent [WebView] creations however.
 *
 * Running this initializer will make sure that this problem doesn't occur in an actual Activity.
 *
 * See [this issue](https://issuetracker.google.com/issues/37124582) for more details.
 */
class SystemThemeMismatchFixInitializer(private val context: Context) {

    fun initialize() {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                WebView(context)
            } catch (e: Exception) {
                // Ignore
            } catch (e: Error) {
                // Ignore
            }
        }
    }
}

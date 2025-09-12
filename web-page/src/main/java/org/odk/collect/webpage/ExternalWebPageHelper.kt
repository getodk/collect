package org.odk.collect.webpage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast

class ExternalWebPageHelper {

    /**
     * Opens web page using Android Custom Tabs. If the user's browser doesn't support Custom Tabs,
     * the Uri will just be opened in their device's default browser.
     */
    fun openWebPage(activity: Activity, uri: Uri) {
        var uri = uri
        uri = uri.normalizeScheme()

        try {
            openUriInCustomTab(activity, uri)
        } catch (_: Exception) {
            openWebPageInBrowser(activity, uri)
        } catch (_: Error) {
            openWebPageInBrowser(activity, uri)
        }
    }

    private fun openWebPageInBrowser(activity: Activity, uri: Uri) {
        var uri = uri
        uri = uri.normalizeScheme()

        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: Exception) {
            showLongToast("No browser installed!")
        } catch (_: Error) {
            showLongToast("No browser installed!")
        }
    }

    private fun openUriInCustomTab(context: Context, uri: Uri) {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    }
}

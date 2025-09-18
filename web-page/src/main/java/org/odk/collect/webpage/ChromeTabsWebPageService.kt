package org.odk.collect.webpage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast

object ChromeTabsWebPageService : WebPageService {

    /**
     * Opens web page using Android Custom Tabs. If the user's browser doesn't support Custom Tabs,
     * the Uri will just be opened in their device's default browser.
     */
    override fun openWebPage(activity: Activity, uri: Uri) {
        val normalizedUri = uri.normalizeScheme()

        try {
            openUriInCustomTab(activity, normalizedUri)
        } catch (_: Throwable) {
            openWebPageInBrowser(activity, normalizedUri)
        }
    }

    private fun openWebPageInBrowser(activity: Activity, uri: Uri) {
        val normalizedUri = uri.normalizeScheme()

        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, normalizedUri))
        } catch (_: Throwable) {
            showLongToast("No browser installed!")
        }
    }

    private fun openUriInCustomTab(context: Context, uri: Uri) {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    }
}

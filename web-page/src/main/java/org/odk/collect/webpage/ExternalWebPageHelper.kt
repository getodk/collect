package org.odk.collect.webpage

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.net.toUri
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast

class ExternalWebPageHelper {

    private var customTabsClient: CustomTabsClient? = null
    private var customTabsSession: CustomTabsSession? = null

    /*
     * unbind 'serviceConnection' after the context in which it was run is destroyed to
     * prevent the leakage of service
     */
    lateinit var serviceConnection: ServiceConnection

    fun bindCustomTabsService(context: Context, url: Uri?) {
        if (customTabsClient != null) {
            return
        }

        serviceConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(
                componentName: ComponentName,
                customTabsClient: CustomTabsClient
            ) {
                this@ExternalWebPageHelper.customTabsClient = customTabsClient
                this@ExternalWebPageHelper.customTabsClient!!.warmup(0L)
                customTabsSession = this@ExternalWebPageHelper.customTabsClient!!.newSession(null)
                if (customTabsSession != null) {
                    customTabsSession!!.mayLaunchUrl(getNonNullUri(url), null, null)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                customTabsClient = null
                customTabsSession = null
            }
        }.also {
            CustomTabsClient.bindCustomTabsService(
                context,
                CUSTOM_TAB_PACKAGE_NAME,
                it
            )
        }
    }

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

    // https://github.com/getodk/collect/issues/1221
    private fun getNonNullUri(url: Uri?): Uri? {
        return url ?: "".toUri()
    }

    companion object {
        const val OPEN_URL: String = "url"
        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
    }
}

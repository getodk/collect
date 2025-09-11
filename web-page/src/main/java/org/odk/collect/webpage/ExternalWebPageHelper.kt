package org.odk.collect.webpage

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast

class ExternalWebPageHelper {

    /**
     * Preload web pages using Custom Tabs so they'll be available faster when [openWebPage] is
     * called.
     */
    fun preloadWebPages(activity: ComponentActivity, uris: List<Uri>) {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            private var serviceConnection: ServiceConnection? = null

            override fun onCreate(owner: LifecycleOwner) {
                serviceConnection = object : CustomTabsServiceConnection() {
                    override fun onCustomTabsServiceConnected(
                        componentName: ComponentName,
                        customTabsClient: CustomTabsClient
                    ) {
                        customTabsClient.warmup(0L)
                        val customTabsSession = customTabsClient.newSession(null)

                        uris.forEach {
                            customTabsSession?.mayLaunchUrl(getNonNullUri(it), null, null)
                        }
                    }

                    override fun onServiceDisconnected(p0: ComponentName?) {
                        // Ignored
                    }
                }.also {
                    CustomTabsClient.bindCustomTabsService(
                        activity,
                        CUSTOM_TAB_PACKAGE_NAME,
                        it
                    )
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                serviceConnection?.also {
                    activity.unbindService(it)
                }
            }
        })
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

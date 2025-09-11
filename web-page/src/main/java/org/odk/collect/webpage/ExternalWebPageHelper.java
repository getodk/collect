package org.odk.collect.webpage;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import org.odk.collect.androidshared.ui.ToastUtils;

/**
 * Created by sanjeev on 17/3/17.
 */
public class ExternalWebPageHelper {

    public static final String OPEN_URL = "url";
    private static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
    private CustomTabsClient customTabsClient;
    private CustomTabsSession customTabsSession;

    /*
     * unbind 'serviceConnection' after the context in which it was run is destroyed to
     * prevent the leakage of service
     */
    private CustomTabsServiceConnection serviceConnection;

    public void bindCustomTabsService(final Context context, final Uri url) {
        if (customTabsClient != null) {
            return;
        }
        serviceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                ExternalWebPageHelper.this.customTabsClient = customTabsClient;
                ExternalWebPageHelper.this.customTabsClient.warmup(0L);
                customTabsSession = ExternalWebPageHelper.this.customTabsClient.newSession(null);
                if (customTabsSession != null) {
                    customTabsSession.mayLaunchUrl(getNonNullUri(url), null, null);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                customTabsClient = null;
                customTabsSession = null;
            }
        };
        CustomTabsClient.bindCustomTabsService(context, CUSTOM_TAB_PACKAGE_NAME, serviceConnection);
    }

    public CustomTabsServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    /**
     * Opens web page using Android Custom Tabs. If the user's browser doesn't support Custom Tabs,
     * the Uri will just be opened in their device's default browser.
     */
    public void openWebPage(Activity activity, Uri uri) {
        uri = uri.normalizeScheme();

        try {
            openUriInCustomTab(activity, uri);
        } catch (Exception | Error e1) {
            openWebPageInBrowser(activity, uri);
        }
    }

    private void openWebPageInBrowser(Activity activity, Uri uri) {
        uri = uri.normalizeScheme();

        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception | Error e2) {
            ToastUtils.showLongToast("No browser installed!");
        }
    }

    private void openUriInCustomTab(Context context, Uri uri) {
        new CustomTabsIntent.Builder().build().launchUrl(context, uri);
    }

    // https://github.com/getodk/collect/issues/1221
    private Uri getNonNullUri(Uri url) {
        return url != null ? url : Uri.parse("");
    }
}

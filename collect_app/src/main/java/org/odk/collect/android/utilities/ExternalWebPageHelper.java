package org.odk.collect.android.utilities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

import org.odk.collect.android.activities.WebViewActivity;

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

    // https://github.com/getodk/collect/issues/1221
    private Uri getNonNullUri(Uri url) {
        return url != null ? url : Uri.parse("");
    }

    public CustomTabsServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public void openWebPageInCustomTab(Activity activity, Uri uri) {
        uri = uri.normalizeScheme();

        try {
            openUriInChromeTabs(activity, uri);
        } catch (Exception | Error e1) {
            openWebPage(activity, uri);
        }
    }

    public void openWebPage(Activity activity, Uri uri) {
        uri = uri.normalizeScheme();

        try {
            openUriInExternalBrowser(activity, uri);
        } catch (Exception | Error e2) {
            openUriInWebView(activity, uri);
        }
    }

    void openUriInChromeTabs(Context context, Uri uri) {
        new CustomTabsIntent.Builder().build().launchUrl(context, uri);
    }

    void openUriInExternalBrowser(Context context, Uri uri) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    void openUriInWebView(Context context, Uri uri) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(OPEN_URL, uri.toString());
        context.startActivity(intent);
    }
}

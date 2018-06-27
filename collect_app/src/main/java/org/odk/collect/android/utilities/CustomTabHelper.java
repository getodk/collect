package org.odk.collect.android.utilities;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import org.odk.collect.android.activities.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanjeev on 17/3/17.
 */
public class CustomTabHelper {
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
                CustomTabHelper.this.customTabsClient = customTabsClient;
                CustomTabHelper.this.customTabsClient.warmup(0L);
                customTabsSession = CustomTabHelper.this.customTabsClient.newSession(null);
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

    /**
     * Code from https://developer.chrome.com/multidevice/android/customtabs
     * For more information see
     * http://stackoverflow.com/a/33281092/137744
     * https://medium.com/google-developers/best-practices-for-custom-tabs-5700e55143ee
     */
    private List<String> getPackageName(Context context) {
        // Get default VIEW intent handler that can view a web url.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.test-url.com"));

        // Get all apps that can handle VIEW intents.
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        return packagesSupportingCustomTabs;
    }

    // https://github.com/opendatakit/collect/issues/1221
    private Uri getNonNullUri(Uri url) {
        return url != null ? url : Uri.parse("");
    }

    public CustomTabsServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public void openUri(Context context, Uri uri) {
        if (!getPackageName(context).isEmpty()) {
            //open in chrome custom tab
            new CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(context, uri);
        } else {
            try {
                //open in external browser
                context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (ActivityNotFoundException e) {
                //open in webview
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(OPEN_URL, uri.toString());
                context.startActivity(intent);
            }
        }
    }
}

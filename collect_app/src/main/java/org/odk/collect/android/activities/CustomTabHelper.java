package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

/**
 * Created by sanjeev on 17/3/17.
 */

public class CustomTabHelper {
    private static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";
    private CustomTabsClient mCustomTabsClient;
    public CustomTabsSession mCustomTabsSession;

    public void bindCustomTabsService( final Activity activity, final Uri url) {
        if (mCustomTabsClient != null) return;
        final CustomTabsServiceConnection mConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
                mCustomTabsSession.mayLaunchUrl(url,null,null);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };
        CustomTabsClient.bindCustomTabsService(activity, CUSTOM_TAB_PACKAGE_NAME, mConnection);
    }
}

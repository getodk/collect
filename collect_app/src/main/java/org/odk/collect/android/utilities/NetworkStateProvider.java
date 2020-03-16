package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.odk.collect.android.application.Collect;

public class NetworkStateProvider {

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) Collect.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentNetworkInfo = manager.getActiveNetworkInfo();
        return currentNetworkInfo != null && currentNetworkInfo.isConnected();
    }
}

package org.odk.collect.android.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.odk.collect.android.application.Collect;

public class NetworkStateProvider {

    public boolean isDeviceOnline() {
        NetworkInfo networkInfo = getNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private NetworkInfo getNetworkInfo() {
        return getConnectivityManager().getActiveNetworkInfo();
    }

    private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) Collect.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}

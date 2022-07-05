package org.odk.collect.androidshared.network;

import android.net.NetworkInfo;

public interface NetworkStateProvider {

    boolean isDeviceOnline();

    NetworkInfo getNetworkInfo();
}

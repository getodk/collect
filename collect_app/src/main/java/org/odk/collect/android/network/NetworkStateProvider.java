package org.odk.collect.android.network;

import android.net.NetworkInfo;

public interface NetworkStateProvider {

    boolean isDeviceOnline();

    NetworkInfo getNetworkInfo();
}

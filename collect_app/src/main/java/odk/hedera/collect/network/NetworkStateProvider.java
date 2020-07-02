package odk.hedera.collect.network;

import android.net.NetworkInfo;

public interface NetworkStateProvider {
    boolean isDeviceOnline();

    NetworkInfo getNetworkInfo();
}

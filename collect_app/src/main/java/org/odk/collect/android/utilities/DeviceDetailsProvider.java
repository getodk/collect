package org.odk.collect.android.utilities;

public interface DeviceDetailsProvider {

    @Deprecated
    String getDeviceId();

    String getLine1Number();

    String getSubscriberId();

    String getSimSerialNumber();
}

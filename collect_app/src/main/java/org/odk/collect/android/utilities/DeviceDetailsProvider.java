package org.odk.collect.android.utilities;

import androidx.annotation.Nullable;

public interface DeviceDetailsProvider {

    @Deprecated
    @Nullable
    String getDeviceId() throws SecurityException;

    @Nullable
    String getLine1Number() throws SecurityException;

    @Nullable
    String getSubscriberId() throws SecurityException;

    @Nullable
    String getSimSerialNumber() throws SecurityException;
}

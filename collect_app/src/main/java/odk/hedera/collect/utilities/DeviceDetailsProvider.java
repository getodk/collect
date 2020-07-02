package odk.hedera.collect.utilities;

import androidx.annotation.Nullable;

public interface DeviceDetailsProvider {

    @Deprecated
    @Nullable
    String getDeviceId();

    @Nullable
    String getLine1Number();

    @Nullable
    String getSubscriberId();

    @Nullable
    String getSimSerialNumber();
}

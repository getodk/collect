package org.odk.collect.android.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import org.odk.collect.android.metadata.InstallIDProvider;

public class StaticCachingDeviceDetailsProvider implements DeviceDetailsProvider {

    /**
     * We want to cache the line number statically as fetching it takes several ms and we don't
     * expect it to change during the process lifecycle.
     */
    private static String lineNumber;
    private static boolean lineNumberFetched;

    private final InstallIDProvider installIDProvider;
    private final Context context;

    public StaticCachingDeviceDetailsProvider(InstallIDProvider installIDProvider, Context context) {
        this.installIDProvider = installIDProvider;
        this.context = context;
    }

    @Override
    public String getDeviceId() {
        return installIDProvider.getInstallID();
    }

    @Override
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getLine1Number() {
        if (!lineNumberFetched) {
            TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            lineNumber = telMgr.getLine1Number();
            lineNumberFetched = true;
        }

        return lineNumber;
    }
}

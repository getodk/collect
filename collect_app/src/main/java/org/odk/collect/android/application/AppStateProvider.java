package org.odk.collect.android.application;

import android.content.Context;

public class AppStateProvider {
    public boolean isFreshInstall(Context context) {
        try {
            long firstInstallTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
            long lastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
            return firstInstallTime == lastUpdateTime;
        } catch (Exception | Error e) {
            return true;
        }
    }
}

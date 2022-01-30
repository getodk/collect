package org.odk.collect.android.utilities;

import static org.odk.collect.settings.ProtectedProjectKeys.KEY_ADMIN_PW;

import org.odk.collect.shared.Settings;

public class AdminPasswordProvider {
    private final Settings adminSettings;

    public AdminPasswordProvider(Settings adminSettings) {
        this.adminSettings = adminSettings;
    }

    public boolean isAdminPasswordSet() {
        String adminPassword = getAdminPassword();
        return adminPassword != null && !adminPassword.isEmpty();
    }

    public String getAdminPassword() {
        return adminSettings.getString(KEY_ADMIN_PW);
    }
}

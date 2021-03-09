package org.odk.collect.android.utilities;

import org.odk.collect.android.preferences.PreferencesDataSource;

import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;

public class AdminPasswordProvider {
    private final PreferencesDataSource adminPrefs;

    public AdminPasswordProvider(PreferencesDataSource adminPrefs) {
        this.adminPrefs = adminPrefs;
    }

    public boolean isAdminPasswordSet() {
        String adminPassword = getAdminPassword();
        return adminPassword != null && !adminPassword.isEmpty();
    }

    public String getAdminPassword() {
        return adminPrefs.getString(KEY_ADMIN_PW);
    }
}

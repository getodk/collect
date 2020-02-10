package org.odk.collect.android.metadata;

import android.content.SharedPreferences;

import static org.odk.collect.utilities.RandomString.randomString;

public class SharedPreferencesInstallIDProvider implements InstallIDProvider {

    private final SharedPreferences sharedPreferences;
    private final String preferencesKey;

    public SharedPreferencesInstallIDProvider(SharedPreferences sharedPreferences, String preferencesKey) {
        this.sharedPreferences = sharedPreferences;
        this.preferencesKey = preferencesKey;
    }

    @Override
    public String getInstallID() {
        if (sharedPreferences.contains(preferencesKey)) {
            return sharedPreferences.getString(preferencesKey, null);
        } else {
            return generateAndStoreInstallID();
        }
    }

    private String generateAndStoreInstallID() {
        String installID = "collect:" + randomString(16);
        sharedPreferences
                .edit()
                .putString(preferencesKey, installID)
                .apply();

        return installID;
    }
}

package org.odk.collect.android.metadata;

import android.annotation.SuppressLint;
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
        if (!sharedPreferences.contains(preferencesKey)) {
            generateInstallID();
        }

        return sharedPreferences.getString(preferencesKey, null);
    }

    @SuppressLint("ApplySharedPref")
    private void generateInstallID() {
        sharedPreferences
                .edit()
                .putString(preferencesKey, "collect:" + randomString(16))
                .commit();
    }
}

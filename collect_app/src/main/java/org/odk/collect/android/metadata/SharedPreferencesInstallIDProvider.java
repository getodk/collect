package org.odk.collect.android.metadata;

import org.odk.collect.android.preferences.PreferencesDataSource;

import static org.odk.collect.utilities.RandomString.randomString;

public class SharedPreferencesInstallIDProvider implements InstallIDProvider {

    private final PreferencesDataSource preferencesDataSource;
    private final String preferencesKey;

    public SharedPreferencesInstallIDProvider(PreferencesDataSource sharedPreferences, String preferencesKey) {
        this.preferencesDataSource = sharedPreferences;
        this.preferencesKey = preferencesKey;
    }

    @Override
    public String getInstallID() {
        if (preferencesDataSource.contains(preferencesKey)) {
            return preferencesDataSource.getString(preferencesKey);
        } else {
            return generateAndStoreInstallID();
        }
    }

    private String generateAndStoreInstallID() {
        String installID = "collect:" + randomString(16);
        preferencesDataSource.save(preferencesKey, installID);

        return installID;
    }
}

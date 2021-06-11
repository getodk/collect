package org.odk.collect.android.metadata;

import org.odk.collect.shared.Settings;

import static org.odk.collect.shared.strings.RandomString.randomString;

public class SharedPreferencesInstallIDProvider implements InstallIDProvider {

    private final Settings metaPreferences;
    private final String preferencesKey;

    public SharedPreferencesInstallIDProvider(Settings metaPreferences, String preferencesKey) {
        this.metaPreferences = metaPreferences;
        this.preferencesKey = preferencesKey;
    }

    @Override
    public String getInstallID() {
        if (metaPreferences.contains(preferencesKey)) {
            return metaPreferences.getString(preferencesKey);
        } else {
            return generateAndStoreInstallID();
        }
    }

    private String generateAndStoreInstallID() {
        String installID = "collect:" + randomString(16);
        metaPreferences.save(preferencesKey, installID);

        return installID;
    }
}

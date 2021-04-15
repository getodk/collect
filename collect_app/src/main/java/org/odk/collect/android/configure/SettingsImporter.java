package org.odk.collect.android.configure;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator;
import org.odk.collect.shared.Settings;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SettingsImporter {

    private final Settings generalSettings;
    private final Settings adminSettings;
    private final SettingsPreferenceMigrator preferenceMigrator;
    private final SettingsValidator settingsValidator;
    private final Map<String, Object> generalDefaults;
    private final Map<String, Object> adminDefaults;
    private final SettingsChangeHandler settingsChangedHandler;

    public SettingsImporter(Settings generalSettings, Settings adminSettings, SettingsPreferenceMigrator preferenceMigrator, SettingsValidator settingsValidator, Map<String, Object> generalDefaults, Map<String, Object> adminDefaults, SettingsChangeHandler settingsChangedHandler) {
        this.generalSettings = generalSettings;
        this.adminSettings = adminSettings;
        this.preferenceMigrator = preferenceMigrator;
        this.settingsValidator = settingsValidator;
        this.generalDefaults = generalDefaults;
        this.adminDefaults = adminDefaults;
        this.settingsChangedHandler = settingsChangedHandler;
    }

    public boolean fromJSON(@NonNull String json) {
        if (!settingsValidator.isValid(json)) {
            return false;
        }

        generalSettings.clear();
        adminSettings.clear();

        try {
            JSONObject jsonObject = new JSONObject(json);

            JSONObject general = jsonObject.getJSONObject("general");
            importToPrefs(general, generalSettings);

            JSONObject admin = jsonObject.getJSONObject("admin");
            importToPrefs(admin, adminSettings);
        } catch (JSONException ignored) {
            // Ignored
        }

        preferenceMigrator.migrate(generalSettings, adminSettings);

        clearUnknownKeys(generalSettings, generalDefaults);
        clearUnknownKeys(adminSettings, adminDefaults);

        loadDefaults(generalSettings, generalDefaults);
        loadDefaults(adminSettings, adminDefaults);

        for (Map.Entry<String, ?> entry: generalSettings.getAll().entrySet()) {
            settingsChangedHandler.onSettingChanged(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, ?> entry: adminSettings.getAll().entrySet()) {
            settingsChangedHandler.onSettingChanged(entry.getKey(), entry.getValue());
        }

        return true;
    }



    private void importToPrefs(JSONObject object, Settings preferences) throws JSONException {
        Iterator<String> generalKeys = object.keys();

        while (generalKeys.hasNext()) {
            String key = generalKeys.next();
            preferences.save(key, object.get(key));
        }
    }

    private void loadDefaults(Settings preferences, Map<String, Object> defaults) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!preferences.contains(entry.getKey())) {
                preferences.save(entry.getKey(), entry.getValue());
            }
        }
    }

    private void clearUnknownKeys(Settings preferences, Map<String, Object> defaults) {
        Set<String> keys = preferences.getAll().keySet();
        for (String key : keys) {
            if (!defaults.containsKey(key)) {
                preferences.remove(key);
            }
        }
    }
}

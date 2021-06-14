package org.odk.collect.android.configure;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.configure.qr.AppConfigurationKeys;

import java.util.Map;

public class StructureAndTypeSettingsValidator implements SettingsValidator {

    private final Map<String, Object> generalDefaults;
    private final Map<String, Object> adminDefaults;

    public StructureAndTypeSettingsValidator(Map<String, Object> generalDefaults, Map<String, Object> adminDefaults) {
        this.generalDefaults = generalDefaults;
        this.adminDefaults = adminDefaults;
    }

    @Override
    public boolean isValid(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject general = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL);
            JSONObject admin = jsonObject.getJSONObject(AppConfigurationKeys.ADMIN);

            return !hasInvalidTypes(general, generalDefaults) && !hasInvalidTypes(admin, adminDefaults);
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean hasInvalidTypes(JSONObject json, Map<String, Object> defaults) throws JSONException {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (json.has(entry.getKey())) {
                Object jsonValue = json.get(entry.getKey());
                Object defaultValue = entry.getValue();

                if (!jsonValue.getClass().isAssignableFrom(defaultValue.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }
}

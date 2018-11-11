package org.odk.collect.android.preferences;

import java.util.Map;

public class PreferenceValidator {

    private final Map<String, Object> defaults;

    public PreferenceValidator(Map<String, Object> defaults) {
        this.defaults = defaults;
    }

    public boolean isValid(Map<String, Object> preferences) {
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            Object defaultValue = entry.getValue();
            Object newValue = preferences.get(entry.getKey());

            if (newValue != null) {
                if (!defaultValue.getClass().isAssignableFrom(newValue.getClass())) {
                    return false;
                }
            }

        }

        return true;
    }
}

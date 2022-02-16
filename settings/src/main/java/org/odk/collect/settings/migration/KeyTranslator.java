package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.replace;

import org.odk.collect.shared.settings.Settings;

import java.util.HashMap;
import java.util.Map;

public class KeyTranslator implements Migration {

    String oldKey;
    String newKey;
    Object tempOldValue;
    Map<Object, Object> translatedValues = new HashMap<>();

    KeyTranslator(String oldKey) {
        this.oldKey = oldKey;
    }

    public KeyTranslator toKey(String newKey) {
        this.newKey = newKey;
        return this;
    }

    public KeyTranslator fromValue(Object oldValue) {
        this.tempOldValue = oldValue;
        return this;
    }

    public KeyTranslator toValue(Object newValue) {
        translatedValues.put(tempOldValue, newValue);
        return this;
    }

    public void apply(Settings prefs) {
        if (prefs.contains(oldKey) && !prefs.contains(newKey)) {
            Object oldValue = prefs.getAll().get(oldKey);
            Object newValue = translatedValues.get(oldValue);
            if (newValue != null) {
                replace(prefs, oldKey, newKey, newValue);
            }
        }
    }
}

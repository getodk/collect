package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import static org.odk.collect.android.utilities.SharedPreferencesUtils.put;

public class KeyExtractor implements Migration {

    private final String newKey;
    private final Map<Object, Object> translatedValues = new HashMap<>();

    private String oldKey;
    private Object tempOldValue;

    public KeyExtractor(String key) {
        this.newKey = key;
    }

    public KeyExtractor fromKey(String key) {
        this.oldKey = key;
        return this;
    }

    public KeyExtractor fromValue(Object oldValue) {
        this.tempOldValue = oldValue;
        return this;
    }

    public KeyExtractor toValue(String newKeyValue) {
        translatedValues.put(tempOldValue, newKeyValue);
        return this;
    }

    @Override
    public void apply(SharedPreferences prefs) {
        if (prefs.contains(newKey)) {
            return;
        }

        Object oldValue = prefs.getAll().get(oldKey);
        SharedPreferences.Editor editor = prefs.edit();
        Object newValue = translatedValues.get(oldValue);

        if (newValue != null) {
            put(editor, newKey, newValue);
            editor.apply();
        }
    }
}

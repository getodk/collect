package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import androidx.core.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static org.odk.collect.android.utilities.SharedPreferencesUtils.put;

public class KeyExtractor implements Migration {

    private final String newKey;
    private final Map<Object, Pair> translatedValues = new HashMap<>();

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

    public KeyExtractor toValues(String oldKeyValue, String newKeyValue) {
        translatedValues.put(tempOldValue, new Pair<>(oldKeyValue, newKeyValue));
        return this;
    }

    @Override
    public void apply(SharedPreferences prefs) {
        if (prefs.contains(newKey)) {
            return;
        }

        Object oldValue = prefs.getAll().get(oldKey);
        SharedPreferences.Editor editor = prefs.edit();
        Pair newValues = translatedValues.get(oldValue);

        if (newValues != null) {
            put(editor, oldKey, newValues.first);
            put(editor, newKey, newValues.second);
            editor.apply();
        }
    }
}

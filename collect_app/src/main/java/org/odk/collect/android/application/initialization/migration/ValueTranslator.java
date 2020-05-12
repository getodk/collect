package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import org.odk.collect.android.preferences.PrefMigrator;

import java.util.Map;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.put;

public class ValueTranslator implements PrefMigrator.Migration {

    private final String oldValue;
    private String newValue;
    private String key;

    public ValueTranslator(String oldValue) {
        this.oldValue = oldValue;
    }

    public ValueTranslator toValue(String newValue) {
        this.newValue = newValue;
        return this;
    }

    public ValueTranslator forKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public void apply(SharedPreferences prefs) {
        if (!prefs.contains(key)) {
            return;
        }

        Map<String, ?> all = prefs.getAll();
        Object prefValue = all.get(key);

        if (prefValue.equals(oldValue)) {
            SharedPreferences.Editor editor = prefs.edit();
            put(editor, key, newValue);
            editor.apply();
        }
    }
}

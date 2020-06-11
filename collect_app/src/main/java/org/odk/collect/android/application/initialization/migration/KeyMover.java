package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import java.util.Map;

import static org.odk.collect.android.utilities.SharedPreferencesUtils.put;

public class KeyMover implements Migration {
    private final String key;
    private SharedPreferences newPrefs;

    public KeyMover(String key) {
        this.key = key;
    }

    public KeyMover toPreferences(SharedPreferences newPrefs) {
        this.newPrefs = newPrefs;
        return this;
    }

    @Override
    public void apply(SharedPreferences prefs) {
        if (newPrefs.contains(key)) {
            return;
        }

        Map<String, ?> all = prefs.getAll();
        Object value = all.get(key);

        prefs.edit()
                .remove(key)
                .apply();

        SharedPreferences.Editor editor = newPrefs.edit();
        put(editor, key, value);
        editor.apply();
    }
}

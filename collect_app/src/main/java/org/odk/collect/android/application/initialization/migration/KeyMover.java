package org.odk.collect.android.application.initialization.migration;

import org.odk.collect.android.preferences.PreferencesDataSource;

import java.util.Map;

public class KeyMover implements Migration {
    private final String key;
    private PreferencesDataSource newPrefs;

    public KeyMover(String key) {
        this.key = key;
    }

    public KeyMover toPreferences(PreferencesDataSource newPrefs) {
        this.newPrefs = newPrefs;
        return this;
    }

    @Override
    public void apply(PreferencesDataSource prefs) {
        if (newPrefs.contains(key)) {
            return;
        }

        Map<String, ?> all = prefs.getAll();
        Object value = all.get(key);

        prefs.remove(key);
        newPrefs.save(key, value);
    }
}

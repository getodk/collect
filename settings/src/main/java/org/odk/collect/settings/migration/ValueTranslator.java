package org.odk.collect.settings.migration;

import org.odk.collect.shared.settings.Settings;

import java.util.Map;

public class ValueTranslator implements Migration {

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
    public void apply(Settings prefs) {
        if (!prefs.contains(key)) {
            return;
        }

        Map<String, ?> all = prefs.getAll();
        Object prefValue = all.get(key);

        if (prefValue.equals(oldValue)) {
            prefs.save(key, newValue);
        }
    }
}

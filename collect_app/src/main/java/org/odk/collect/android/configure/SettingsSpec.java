package org.odk.collect.android.configure;

import java.util.Map;
import java.util.Set;

public class SettingsSpec {

    private final Map<String, Object> defaults;

    public SettingsSpec(Map<String, Object> defaults) {
        this.defaults = defaults;
    }

    public Object getDefault(String key) {
        return defaults.get(key);
    }

    public Set<String> getKeys() {
        return defaults.keySet();
    }

    public boolean isValid(String key, Object value) {
        return defaults.get(key).getClass().isAssignableFrom(value.getClass());
    }

    public boolean hasKey(String key) {
        return defaults.containsKey(key);
    }
}

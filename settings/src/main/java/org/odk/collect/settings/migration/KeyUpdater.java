package org.odk.collect.settings.migration;

import static org.odk.collect.settings.migration.MigrationUtils.asPairs;

import org.odk.collect.shared.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KeyUpdater implements Migration {

    String[] oldKeys;
    Object[] tempOldValueArray;
    List<Object[]> oldValueArrays = new ArrayList<>();
    List<KeyValuePair[]> newKeyValuePairArrays = new ArrayList<>();

    KeyUpdater(String... oldKeys) {
        this.oldKeys = oldKeys;
    }

    public KeyUpdater withValues(Object... oldValues) {
        tempOldValueArray = oldValues;
        return this;
    }

    public KeyUpdater toPairs(Object... keysAndValues) {
        oldValueArrays.add(tempOldValueArray);
        newKeyValuePairArrays.add(asPairs(keysAndValues));
        return this;
    }

    public void apply(Settings prefs) {
        Map<String, ?> prefMap = prefs.getAll();
        Object[] oldValues = new Object[oldKeys.length];
        for (int i = 0; i < oldKeys.length; i++) {
            oldValues[i] = prefMap.get(oldKeys[i]);
        }
        for (int i = 0; i < oldValueArrays.size(); i++) {
            if (Arrays.equals(oldValues, oldValueArrays.get(i))) {
                for (KeyValuePair keyValuePair : newKeyValuePairArrays.get(i)) {
                    prefs.save(keyValuePair.key, keyValuePair.value);
                }
            }
        }
    }
}

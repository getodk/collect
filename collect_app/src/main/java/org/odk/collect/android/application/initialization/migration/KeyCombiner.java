package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.odk.collect.android.application.initialization.migration.MigrationUtils.asPairs;
import static org.odk.collect.android.application.initialization.migration.MigrationUtils.replace;

public class KeyCombiner implements Migration {

    String[] oldKeys;
    Object[] tempOldValueArray;
    List<Object[]> oldValueArrays = new ArrayList<>();
    List<Pair[]> newPairArrays = new ArrayList<>();

    KeyCombiner(String... oldKeys) {
        this.oldKeys = oldKeys;
    }

    public KeyCombiner withValues(Object... oldValues) {
        tempOldValueArray = oldValues;
        return this;
    }

    public KeyCombiner toPairs(Object... keysAndValues) {
        oldValueArrays.add(tempOldValueArray);
        newPairArrays.add(asPairs(keysAndValues));
        return this;
    }

    public void apply(SharedPreferences prefs) {
        Map<String, ?> prefMap = prefs.getAll();
        Object[] oldValues = new Object[oldKeys.length];
        for (int i = 0; i < oldKeys.length; i++) {
            oldValues[i] = prefMap.get(oldKeys[i]);
        }
        for (int i = 0; i < oldValueArrays.size(); i++) {
            if (Arrays.equals(oldValues, oldValueArrays.get(i))) {
                replace(prefs, oldKeys, newPairArrays.get(i));
            }
        }
    }
}

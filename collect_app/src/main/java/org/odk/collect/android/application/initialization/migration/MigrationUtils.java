package org.odk.collect.android.application.initialization.migration;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

public class MigrationUtils {

    private MigrationUtils() {

    }

    public static ValueTranslator translateValue(String value) {
        return new ValueTranslator(value);
    }

    /**
     * A migration that moves the value of an old preference key over to a new key.
     * Removes the old key and writes the new key ONLY if the old key is set and
     * the new key is not set.  Example:
     *         renameKey("color").toKey("couleur")
     */
    public static KeyRenamer renameKey(String oldKey) {
        return new KeyRenamer(oldKey);
    }

    /**
     * A migration that replaces an old preference key with a new key, translating
     * specific old values to specific new values.  Removes the old key and writes
     * the new key ONLY if the old key's value exactly matches one of the values
     * passed to fromValue() and the new key is not set.  Example:
     *         translateKey("color").toKey("couleur")
     *             .fromValue("red").toValue("rouge")
     *             .fromValue("yellow").toValue("jaune")
     */
    public static KeyTranslator translateKey(String oldKey) {
        return new KeyTranslator(oldKey);
    }

    /**
     * A migration that combines multiple keys by looking for specific sets of
     * old values across multiple keys, and replacing them with new key-value pairs.
     * Removes the old keys and writes the new key-value pairs ONLY if all the
     * values of the old keys match the set of values passed to withValues().
     * New key-value pairs MAY overwrite existing keys.  Example:
     *         combineKeys("colour", "op")
     *             .withValues("red", "lighten")
     *                 .toPairs("colour", "pink")  // only if hue = red AND op = lighten
     *             .withValues("yellow", "darken")
     *                 .toPairs("color", "brown")  // only if hue = yellow AND op = darken
     */
    public static KeyCombiner combineKeys(String... oldKeys) {
        return new KeyCombiner(oldKeys);
    }

    public static KeyMover moveKey(String key) {
        return new KeyMover(key);
    }

    public static Migration removeKey(String key) {
        return prefs -> prefs.edit().remove(key).apply();
    }

    /** Removes an old key and sets a new key. */
    @SuppressLint("ApplySharedPref")
    static void replace(SharedPreferences prefs, String oldKey, String newKey, Object newValue) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(oldKey);
        put(editor, newKey, newValue);
        editor.commit();
    }

    /** Removes one or more old keys, then adds one or more new key-value pairs. */
    @SuppressLint("ApplySharedPref")
    static void replace(SharedPreferences prefs, String[] oldKeys, Pair... newPairs) {
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : oldKeys) {
            editor.remove(key);
        }
        for (Pair pair : newPairs) {
            put(editor, pair.key, pair.value);
        }
        editor.commit();
    }

    /** Writes a key with a value of varying type to a SharedPreferences.Editor. */
    static void put(SharedPreferences.Editor editor, String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
    }

    /** Converts an array of alternating keys and values into an array of Pairs. */
    static Pair[] asPairs(Object... args) {
        Pair[] pairs = new Pair[args.length / 2];
        for (int i = 0; i * 2 + 1 < args.length; i++) {
            pairs[i] = new Pair((String) args[i * 2], args[i * 2 + 1]);
        }
        return pairs;
    }
}
package org.odk.collect.android.application.initialization.migration;

/** A single preference setting, consisting of a String key and a value of varying type. */
class Pair {
    final String key;
    final Object value;

    Pair(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}

package org.odk.collect.settings.migration;

/** A single preference setting, consisting of a String key and a value of varying type. */
class KeyValuePair {
    final String key;
    final Object value;

    KeyValuePair(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}

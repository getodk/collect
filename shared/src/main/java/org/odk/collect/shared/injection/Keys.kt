package org.odk.collect.shared.injection

/**
 * Container for named keys. Useful for injecting keys into modules where using a String or Map
 * types would be awkward.
 */
class Keys(private val keys: Map<String, String>) {
    fun get(name: String): String? {
        return keys[name]
    }
}

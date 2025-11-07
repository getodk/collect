package org.odk.collect.androidshared.utils

class RuntimeUniqueIdGenerator() : UniqueIdGenerator {

    private val ids = mutableMapOf<String, Int>()
    private var next = 1

    override fun getInt(identifier: String): Int {
        return synchronized(this) {
            ids.getOrPut(identifier) { next++ }
        }
    }
}

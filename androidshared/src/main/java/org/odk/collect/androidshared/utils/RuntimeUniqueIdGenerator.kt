package org.odk.collect.androidshared.utils

object RuntimeUniqueIdGenerator : UniqueIdGenerator {

    private var next = 1

    override fun getInt(): Int {
        return synchronized(this) {
            next++
        }
    }
}

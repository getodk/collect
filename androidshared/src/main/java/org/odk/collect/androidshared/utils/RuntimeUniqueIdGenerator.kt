package org.odk.collect.androidshared.utils

object RuntimeUniqueIdGenerator {

    private var next = 1

    fun nextInt(): Int {
        return synchronized(this) {
            next++
        }
    }
}

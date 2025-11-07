package org.odk.collect.androidshared.utils

import java.util.concurrent.atomic.AtomicInteger

class InMemUniqueIdGenerator() : UniqueIdGenerator {

    private val ids = mutableMapOf<String, Int>()
    private var next = AtomicInteger(1)

    override fun getInt(identifier: String): Int {
        return ids.getOrPut(identifier) { next.getAndIncrement() }
    }
}

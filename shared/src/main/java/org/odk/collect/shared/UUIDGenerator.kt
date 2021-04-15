package org.odk.collect.shared

import java.util.UUID

class UUIDGenerator {
    fun generateUUID() = UUID.randomUUID().toString()
}

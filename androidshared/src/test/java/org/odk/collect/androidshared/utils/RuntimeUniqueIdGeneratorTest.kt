package org.odk.collect.androidshared.utils

class RuntimeUniqueIdGeneratorTest : UniqueIdGeneratorTest() {
    override fun buildSubject(): UniqueIdGenerator {
        return RuntimeUniqueIdGenerator()
    }
}

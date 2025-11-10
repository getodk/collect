package org.odk.collect.androidshared.utils

class InMemUniqueIdGeneratorTest : UniqueIdGeneratorTest() {
    override fun buildSubject(): UniqueIdGenerator {
        return InMemUniqueIdGenerator()
    }
}

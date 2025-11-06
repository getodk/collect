package org.odk.collect.androidshared.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

abstract class UniqueIdGeneratorTest {

    abstract fun buildSubject(): UniqueIdGenerator

    @Test
    fun `returns an incrementing unique ID for each identifier`() {
        val generator = buildSubject()
        val id1 = generator.getInt("id1")
        val id2 = generator.getInt("id2")

        assertThat(id2, equalTo(id1 + 1))
    }

    @Test
    fun `returns a consistent ID for an identifier`() {
        val generator = buildSubject()
        val id1 = generator.getInt("id1")
        val id2 = generator.getInt("id1")

        assertThat(id2, equalTo(id1))
    }
}

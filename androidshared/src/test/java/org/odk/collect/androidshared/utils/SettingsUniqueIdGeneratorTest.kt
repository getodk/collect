package org.odk.collect.androidshared.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.shared.settings.Settings

class SettingsUniqueIdGeneratorTest : UniqueIdGeneratorTest() {
    override fun buildSubject(): UniqueIdGenerator {
        return buildGenerator(InMemSettings())
    }

    @Test
    fun `ids are consistent between instances`() {
        val settings = InMemSettings()
        val generator1 = buildGenerator(settings)
        val generator2 = buildGenerator(settings)

        // Initialize IDs in different orders
        generator1.getInt("id1")
        generator1.getInt("id2")
        generator2.getInt("id2")
        generator2.getInt("id1")

        assertThat(generator1.getInt("id1"), equalTo(generator2.getInt("id1")))
    }

    @Test
    fun `ids increment between instances`() {
        val settings = InMemSettings()
        val generator1 = buildGenerator(settings)
        val generator2 = buildGenerator(settings)

        val id1 = generator1.getInt("id1")
        val id2 = generator2.getInt("id2")
        assertThat(id2, equalTo(id1 + 1))
    }

    private fun buildGenerator(settings: Settings): SettingsUniqueIdGenerator =
        SettingsUniqueIdGenerator(settings)
}

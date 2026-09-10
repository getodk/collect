package org.odk.collect.settings.validation

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class OriginalJsonSchemaSettingsValidatorTest {

    /*
     * Some settings and setting values end up replaced by new ones but we need the schema to
     * still recognize the old ones so that we can migrate them correctly.
     */
    @Test
    fun `isValueSupported returns true for settings and values we no longer use`() {
        val validator = JsonSchemaSettingsValidator {
            javaClass.getResourceAsStream("/client-settings.schema.json")!!
        }

        removedSettingsAndValues.forEach {
            assertThat(
                validator.isValueSupported(it.first, it.second, it.third),
                equalTo(true)
            )
        }
    }

    private val removedSettingsAndValues = listOf(
        Triple("admin", "mark_as_finalized", "true"),
        Triple("general", "default_completed", "true"),
        Triple("admin", "finalize", "true"),
        Triple("general", "basemap_source", "mapbox")
    )
}

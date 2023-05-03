package org.odk.collect.settings.validation

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class OriginalJsonSchemaSettingsValidatorTest {

    /*
     * 'default_completed' and 'mark_as_finalized' were replaced by new settings in v2023.2 but
     * we need the schema to still recognize the old fields so that we can migrate them correctly.
     */
    @Test
    fun `isValueSupported returns true for fields we no longer use`() {
        val validator = JsonSchemaSettingsValidator {
            javaClass.getResourceAsStream("/client-settings.schema.json")!!
        }

        assertThat(
            validator.isValueSupported("general", "default_completed", "true"),
            equalTo(true)
        )

        assertThat(
            validator.isValueSupported("admin", "mark_as_finalized", "true"),
            equalTo(true)
        )
    }
}

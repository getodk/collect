package org.odk.collect.settings.validation

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

class JsonSchemaSettingsValidatorTest {
    @Test
    fun `isValid returns true when json is valid based on schema`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid(
                """
                    {
                        "general":{
                            "foo":"option1"
                        }
                    }
                """
            ),
            equalTo(true)
        )
    }

    @Test
    fun `isValid returns false when json is invalid based on schema`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid(
                """
                    {
                        "general":{
                            "foo":false
                        }
                    }
                """
            ),
            equalTo(false)
        )
    }

    @Test
    fun `isValid returns false when json is invalid`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid("*"),
            equalTo(false)
        )
    }

    @Test
    fun `isValid returns true when json contains values different than those specified in a corresponding enum`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValid(
                """
                    {
                        "general":{
                            "foo":"option3"
                        }
                    }
                """
            ),
            equalTo(true)
        )
    }

    @Test
    fun `isKeySupported returns true for keys allowed by the scheme`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isKeySupported("general", "foo"),
            equalTo(true)
        )
    }

    @Test
    fun `isKeySupported returns false for keys not allowed by the scheme`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isKeySupported("general", "baz"),
            equalTo(false)
        )
    }

    @Test
    fun `isValueSupported returns true for values allowed by the scheme`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValueSupported("general", "foo", "option1"),
            equalTo(true)
        )
    }

    @Test
    fun `isValueSupported returns false for values not allowed by the scheme`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValueSupported("general", "foo", "option3"),
            equalTo(false)
        )
    }

    @Test
    fun `isValueSupported returns true for any value when the scheme does not specify allowed values`() {
        val validator = JsonSchemaSettingsValidator {
            SCHEMA.byteInputStream()
        }

        assertThat(
            validator.isValueSupported("general", "bar", "option3"),
            equalTo(true)
        )
    }
}

private const val SCHEMA = """
            {
                "${'$'}schema": "https://json-schema.org/draft/2019-09/schema",
                "${'$'}id": "https://example.com/example.schema.json",
                "title": "Schema",
                "type": "object",
                "properties": {
                    "general": {
                        "type": "object",
                        "properties": {
                            "foo": {
                                "type": "string",
                                "enum": [
                                    "option1",
                                    "option2"
                                ]
                            },
                            "bar": {
                                "type": "string"
                            }
                        }
                    }
                }
            }
            """

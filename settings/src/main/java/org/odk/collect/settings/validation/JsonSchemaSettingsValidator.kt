package org.odk.collect.settings.validation

import com.github.fge.jackson.JsonNodeReader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.odk.collect.settings.importing.SettingsValidator
import java.io.InputStream
import java.io.StringReader

internal class JsonSchemaSettingsValidator(private val schemaProvider: () -> InputStream) :
    SettingsValidator {

    override fun isValid(json: String): Boolean {
        return schemaProvider().use { schemaStream ->
            StringReader(json).use {
                JsonSchemaFactory
                    .byDefault()
                    .getJsonSchema(JsonNodeReader().fromInputStream(schemaStream))
                    .validate(JsonNodeReader().fromReader(it))
                    .isSuccess
            }
        }
    }
}

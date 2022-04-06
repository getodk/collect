package org.odk.collect.settings.validation

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import org.odk.collect.settings.importing.SettingsValidator
import java.io.InputStream

internal class JsonSchemaSettingsValidator(private val schemaProvider: () -> InputStream) :
    SettingsValidator {

    override fun isValid(json: String): Boolean {
        return try {
            schemaProvider().use { schemaStream ->
                val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                val schema = schemaFactory.getSchema(schemaStream)
                val errors = schema.validate(ObjectMapper().readTree(json))
                errors.isEmpty()
            }
        } catch (e: JsonParseException) {
            false
        }
    }
}

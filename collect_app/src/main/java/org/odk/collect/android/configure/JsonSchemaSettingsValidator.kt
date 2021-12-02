package org.odk.collect.android.configure

import com.github.fge.jackson.JsonNodeReader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import java.io.InputStream
import java.io.StringReader

class JsonSchemaSettingsValidator(private val schemaProvider: () -> InputStream) : SettingsValidator {

    override fun isValid(json: String): Boolean {
        schemaProvider().use { schemaStream ->
            val jsonNode = JsonNodeReader().fromInputStream(schemaStream)
            val jsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(jsonNode)

            StringReader(json).use {
                val report = jsonSchema.validate(JsonNodeReader().fromReader(it))
                return report.isSuccess
            }
        }
    }
}

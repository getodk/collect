package org.odk.collect.settings.validation

import com.networknt.schema.Schema
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.dialect.Dialects
import com.networknt.schema.keyword.KeywordType
import org.json.JSONObject
import org.odk.collect.settings.importing.SettingsValidator
import org.odk.collect.shared.collections.CollectionExtensions.has
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.ObjectMapper
import java.io.InputStream
import java.util.Map

internal class JsonSchemaSettingsValidator(private val schemaProvider: () -> InputStream) :
    SettingsValidator {

    private val schemaString: String by lazy {
        schemaProvider().bufferedReader().use { it.readText() }
    }

    private val schemaJsonObject: JSONObject by lazy {
        JSONObject(schemaString)
    }

    override fun isValid(json: String): Boolean {
        return try {
            val schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft201909()) {
                it.schemas(
                    Map.of(
                        "https://example.com/address.schema.json",
                        schemaString
                    )
                )
            }
            val schema: Schema =
                schemaRegistry.getSchema(SchemaLocation.of("https://example.com/address.schema.json"))

            val errors = schema.validate(ObjectMapper().readTree(json))
            errors.none { it.keyword != KeywordType.ENUM.value }
        } catch (_: StreamReadException) {
            false
        }
    }

    override fun isKeySupported(parentJsonObjectName: String, key: String): Boolean {
        return try {
            return schemaJsonObject
                .getJSONObject("properties")
                .getJSONObject(parentJsonObjectName)
                .getJSONObject("properties")
                .has(key)
        } catch (_: StreamReadException) {
            false
        }
    }

    override fun isValueSupported(parentJsonObjectName: String, key: String, value: Any): Boolean {
        return try {
            val settingJsonObject = schemaJsonObject
                .getJSONObject("properties")
                .getJSONObject(parentJsonObjectName)
                .getJSONObject("properties")
                .getJSONObject(key)

            return if (settingJsonObject.has("enum")) {
                settingJsonObject.getJSONArray("enum").has(value)
            } else {
                true
            }
        } catch (_: StreamReadException) {
            false
        }
    }
}

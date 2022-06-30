package org.odk.collect.settings.validation

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidatorTypeCode
import org.json.JSONObject
import org.odk.collect.settings.importing.SettingsValidator
import java.io.InputStream

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
            val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
            val schema = schemaFactory.getSchema(schemaString)
            val errors = schema.validate(ObjectMapper().readTree(json))
            errors.none { it.type != ValidatorTypeCode.ENUM.value }
        } catch (e: JsonParseException) {
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
        } catch (e: JsonParseException) {
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
                val supportedValues = settingJsonObject.getJSONArray("enum")

                for (i in 0 until supportedValues.length()) {
                    if (supportedValues[i] == value) {
                        return true
                    }
                }
                false
            } else {
                true
            }
        } catch (e: JsonParseException) {
            false
        }
    }
}

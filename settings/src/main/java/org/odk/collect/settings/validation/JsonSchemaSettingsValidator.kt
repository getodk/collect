package org.odk.collect.settings.validation

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidatorTypeCode
import org.json.JSONObject
import org.odk.collect.settings.importing.SettingsValidator
import java.io.InputStream
import java.nio.charset.Charset

internal class JsonSchemaSettingsValidator(private val schemaProvider: () -> InputStream) :
    SettingsValidator {

    override fun isValid(json: String): Boolean {
        return try {
            schemaProvider().use { schemaStream ->
                val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                val schema = schemaFactory.getSchema(schemaStream)
                val errors = schema.validate(ObjectMapper().readTree(json))
                errors.none { it.type != ValidatorTypeCode.ENUM.value }
            }
        } catch (e: JsonParseException) {
            false
        }
    }

    override fun isKeySupported(parentJsonObjectName: String, key: String): Boolean {
        return try {
            schemaProvider().use { schemaStream ->
                val schemaJsonObject = JSONObject(
                    schemaStream.readBytes().toString(Charset.defaultCharset())
                )

                return schemaJsonObject
                    .getJSONObject("properties")
                    .getJSONObject(parentJsonObjectName)
                    .getJSONObject("properties")
                    .has(key)
            }
        } catch (e: JsonParseException) {
            false
        }
    }

    override fun isValueSupported(parentJsonObjectName: String, key: String, value: Any): Boolean {
        return try {
            schemaProvider().use { schemaStream ->
                val schemaJsonObject = JSONObject(
                    schemaStream.readBytes().toString(Charset.defaultCharset())
                )

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
            }
        } catch (e: JsonParseException) {
            false
        }
    }
}

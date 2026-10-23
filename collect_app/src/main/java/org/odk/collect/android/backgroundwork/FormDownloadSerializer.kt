package org.odk.collect.android.backgroundwork

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.odk.collect.android.formmanagement.EntityListDownload
import org.odk.collect.android.formmanagement.FormDownload
import org.odk.collect.android.formmanagement.MediaFilesDownload
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.MediaFile
import java.io.File

object FormDownloadSerializer {

    private val gson = Gson()

    fun serialize(formDownload: FormDownload): String {
        val json = JsonObject()

        when (formDownload) {
            is FormDownload.New -> {
                json.addProperty("type", "New")
                json.addProperty("file", formDownload.file.absolutePath)
                json.addProperty("hash", formDownload.hash)
                json.addProperty("tempDir", formDownload.tempDir.absolutePath)
                json.add(
                    "mediaFilesDownload",
                    serializeMediaFilesDownload(formDownload.mediaFilesDownload)
                )
            }

            is FormDownload.Existing -> {
                json.addProperty("type", "Existing")

                if (formDownload.form.dbId != null) {
                    json.addProperty("formDbId", formDownload.form.dbId)
                } else {
                    throw IllegalArgumentException()
                }

                json.addProperty("tempDir", formDownload.tempDir.absolutePath)
                json.add(
                    "mediaFilesDownload",
                    serializeMediaFilesDownload(formDownload.mediaFilesDownload)
                )
            }
        }

        return json.toString()
    }

    fun deserialize(json: String, formsRepository: FormsRepository): FormDownload {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        val type = jsonObject.get("type").asString

        return when (type) {
            "New" -> {
                val file = File(jsonObject.get("file").asString)
                val hash = jsonObject.get("hash").asString
                val tempDir = File(jsonObject.get("tempDir").asString)
                val mediaFilesDownload =
                    deserializeMediaFilesDownload(jsonObject.getAsJsonObject("mediaFilesDownload"))
                FormDownload.New(file, hash, tempDir, mediaFilesDownload)
            }

            "Existing" -> {
                val formDbId =
                    if (jsonObject.has("formDbId") && !jsonObject.get("formDbId").isJsonNull) {
                        jsonObject.get("formDbId").asLong
                    } else {
                        throw IllegalStateException("Form dbId is null")
                    }
                val form = formsRepository.get(formDbId)
                    ?: throw IllegalStateException("Form with dbId $formDbId not found")
                val tempDir = File(jsonObject.get("tempDir").asString)
                val mediaFilesDownload =
                    deserializeMediaFilesDownload(jsonObject.getAsJsonObject("mediaFilesDownload"))
                FormDownload.Existing(form, tempDir, mediaFilesDownload)
            }

            else -> throw IllegalArgumentException("Unknown FormDownload type: $type")
        }
    }

    private fun serializeMediaFilesDownload(mediaFilesDownload: MediaFilesDownload): JsonObject {
        val json = JsonObject()
        json.addProperty("tempMediaPath", mediaFilesDownload.tempMediaPath)
        json.addProperty("newAttachmentsDownloaded", mediaFilesDownload.newAttachmentsDownloaded)
        json.add(
            "entityLists",
            gson.toJsonTree(mediaFilesDownload.entityLists.map { serializeEntityListDownload(it) })
        )
        return json
    }

    private fun deserializeMediaFilesDownload(json: JsonObject): MediaFilesDownload {
        val tempMediaPath = json.get("tempMediaPath").asString
        val newAttachmentsDownloaded = json.get("newAttachmentsDownloaded").asBoolean
        val entityListsJson = json.getAsJsonArray("entityLists")
        val entityLists = entityListsJson.map { deserializeEntityListDownload(it.asJsonObject) }
        return MediaFilesDownload(tempMediaPath, newAttachmentsDownloaded, entityLists)
    }

    private fun serializeEntityListDownload(entityListDownload: EntityListDownload): Map<String, Any?> {
        return when (entityListDownload) {
            is EntityListDownload.Update -> {
                mapOf(
                    "type" to "Update",
                    "mediaFile" to mapOf(
                        "filename" to entityListDownload.mediaFile.filename,
                        "hash" to entityListDownload.mediaFile.hash,
                        "downloadUrl" to entityListDownload.mediaFile.downloadUrl,
                        "type" to entityListDownload.mediaFile.type?.name,
                        "integrityUrl" to entityListDownload.mediaFile.integrityUrl
                    ),
                    "file" to entityListDownload.file.absolutePath
                )
            }

            is EntityListDownload.Skipped -> {
                mapOf(
                    "type" to "Skipped",
                    "mediaFile" to mapOf(
                        "filename" to entityListDownload.mediaFile.filename,
                        "hash" to entityListDownload.mediaFile.hash,
                        "downloadUrl" to entityListDownload.mediaFile.downloadUrl,
                        "type" to entityListDownload.mediaFile.type?.name,
                        "integrityUrl" to entityListDownload.mediaFile.integrityUrl
                    )
                )
            }
        }
    }

    private fun deserializeEntityListDownload(json: JsonObject): EntityListDownload {
        val type = json.get("type").asString
        val mediaFileJson = json.getAsJsonObject("mediaFile")
        val mediaFile = MediaFile(
            filename = mediaFileJson.get("filename").asString,
            hash = mediaFileJson.get("hash").asString,
            downloadUrl = mediaFileJson.get("downloadUrl").asString,
            type = if (mediaFileJson.has("type") && !mediaFileJson.get("type").isJsonNull) {
                MediaFile.Type.valueOf(mediaFileJson.get("type").asString)
            } else {
                null
            },
            integrityUrl = if (mediaFileJson.has("integrityUrl") && !mediaFileJson.get("integrityUrl").isJsonNull) {
                mediaFileJson.get("integrityUrl").asString
            } else {
                null
            }
        )

        return when (type) {
            "Update" -> {
                val file = File(json.get("file").asString)
                EntityListDownload.Update(mediaFile, file)
            }

            "Skipped" -> {
                EntityListDownload.Skipped(mediaFile)
            }

            else -> throw IllegalArgumentException("Unknown EntityListDownload type: $type")
        }
    }
}

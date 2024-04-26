package org.odk.collect.android.entities

import android.os.StrictMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.Entity
import java.io.File

class JsonFileEntitiesRepository(directory: File) : EntitiesRepository {

    private val entitiesFile = File(directory, "entities.json")

    override fun getDatasets(): Set<String> {
        return readJson().keys
    }

    override fun getEntities(dataset: String): List<Entity> {
        return readEntities().filter { it.dataset == dataset }
    }

    override fun save(entity: Entity) {
        val entities = readEntities()
        val existing = entities.find { it.id == entity.id && it.dataset == entity.dataset }

        if (existing != null) {
            entities.remove(existing)
            entities.add(
                Entity(
                    entity.dataset,
                    entity.id,
                    entity.label ?: existing.label,
                    version = entity.version,
                    properties = mergeProperties(existing, entity)
                )
            )
        } else {
            entities.add(entity)
        }

        writeEntities(entities)
    }

    override fun clear() {
        StrictMode.noteSlowCall("Writing to JSON file")
        entitiesFile.delete()
    }

    override fun addDataset(dataset: String) {
        val existing = readJson()
        if (!existing.containsKey(dataset)) {
            existing[dataset] = mutableListOf()
        }

        writeJson(existing)
    }

    private fun writeEntities(entities: MutableList<Entity>) {
        val map = mutableMapOf<String, MutableList<JsonEntity>>()
        entities.forEach {
            map.getOrPut(it.dataset) { mutableListOf() }.add(it.toJson())
        }

        writeJson(map)
    }

    private fun readEntities(): MutableList<Entity> {
        return readJson().entries.flatMap { (dataset, entities) ->
            entities.map { it.toEntity(dataset) }
        }.toMutableList()
    }

    private fun readJson(): MutableMap<String, MutableList<JsonEntity>> {
        StrictMode.noteSlowCall("Reading from JSON file")

        if (!entitiesFile.exists()) {
            entitiesFile.parentFile.mkdirs()
            entitiesFile.createNewFile()
        }

        val typeToken = TypeToken.getParameterized(
            MutableMap::class.java,
            String::class.java,
            TypeToken.getParameterized(MutableList::class.java, JsonEntity::class.java).type
        )

        val json = entitiesFile.readText()
        return if (json.isNotBlank()) {
            val parsedJson =
                Gson().fromJson<MutableMap<String, MutableList<JsonEntity>>>(json, typeToken.type)

            parsedJson
        } else {
            mutableMapOf()
        }
    }

    private fun writeJson(map: MutableMap<String, MutableList<JsonEntity>>) {
        StrictMode.noteSlowCall("Writing to JSON file")

        val json = Gson().toJson(map)
        entitiesFile.writeText(json)
    }

    private fun mergeProperties(
        existing: Entity,
        new: Entity
    ): List<Pair<String, String>> {
        val existingProperties = mutableMapOf(*existing.properties.toTypedArray())
        new.properties.forEach {
            existingProperties[it.first] = it.second
        }

        return existingProperties.map { Pair(it.key, it.value) }
    }

    private data class JsonEntity(
        val id: String,
        val label: String?,
        val version: Int = 1,
        val properties: Map<String, String>
    )

    private fun JsonEntity.toEntity(dataset: String): Entity {
        return Entity(
            dataset,
            this.id,
            this.label,
            this.version,
            this.properties.entries.map { Pair(it.key, it.value) }
        )
    }

    private fun Entity.toJson(): JsonEntity {
        return JsonEntity(
            this.id,
            this.label,
            this.version,
            this.properties.toMap()
        )
    }
}

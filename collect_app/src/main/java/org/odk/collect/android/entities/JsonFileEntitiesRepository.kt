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
        return readEntities().map { it.dataset }.toSet()
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

    private fun writeEntities(entities: MutableList<Entity>) {
        StrictMode.noteSlowCall("Writing to JSON file")

        val json = Gson().toJson(entities.map { it.toJson() })
        entitiesFile.writeText(json)
    }

    private fun readEntities(): MutableList<Entity> {
        StrictMode.noteSlowCall("Reading from JSON file")

        if (!entitiesFile.exists()) {
            entitiesFile.parentFile.mkdirs()
            entitiesFile.createNewFile()
        }

        val typeToken = TypeToken.getParameterized(MutableList::class.java, JsonEntity::class.java)
        return (Gson().fromJson<MutableList<JsonEntity>>(entitiesFile.readText(), typeToken.type) ?: emptyList())
            .map { it.toEntity() }
            .toMutableList()
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
        val dataset: String,
        val id: String,
        val label: String?,
        val version: Int = 1,
        val properties: Map<String, String>
    )

    private fun JsonEntity.toEntity(): Entity {
        return Entity(
            this.dataset,
            this.id,
            this.label,
            this.version,
            this.properties.entries.map { Pair(it.key, it.value) }
        )
    }

    private fun Entity.toJson(): JsonEntity {
        return JsonEntity(
            this.dataset,
            this.id,
            this.label,
            this.version,
            this.properties.toMap()
        )
    }
}

package org.odk.collect.android.entities

import android.os.StrictMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity
import java.io.File

class JsonFileEntitiesRepository(directory: File) : EntitiesRepository {

    private val entitiesFile = File(directory, "entities.json")

    override fun getLists(): Set<String> {
        return readJson().keys
    }

    override fun getEntities(list: String): List<Entity.Saved> {
        return readEntities().filter { it.list == list }.mapIndexed { index, entity ->
            Entity.Saved(
                entity.list,
                entity.id,
                entity.label,
                entity.version,
                entity.properties,
                entity.state,
                index,
                entity.trunkVersion
            )
        }
    }

    override fun save(vararg entities: Entity) {
        val json = readJson()

        entities.forEach { entity ->
            val entityList = json.getOrPut(entity.list) { mutableListOf() }
            val existing = json.values.flatten().find { it.id == entity.id }

            if (existing != null) {
                val state = if (existing.offline) {
                    entity.state
                } else {
                    Entity.State.ONLINE
                }

                entityList.remove(existing)
                entityList.add(
                    Entity.New(
                        entity.list,
                        entity.id,
                        entity.label ?: existing.label,
                        version = entity.version,
                        properties = mergeProperties(existing.toEntity(entity.list), entity),
                        state = state,
                        trunkVersion = entity.trunkVersion
                    ).toJson()
                )
            } else {
                entityList.add(entity.toJson())
            }
        }

        writeJson(json)
    }

    override fun clear() {
        StrictMode.noteSlowCall("Writing to JSON file")
        entitiesFile.delete()
    }

    override fun addList(list: String) {
        val existing = readJson()
        if (!existing.containsKey(list)) {
            existing[list] = mutableListOf()
        }

        writeJson(existing)
    }

    override fun delete(id: String) {
        val existing = readEntities()
        existing.removeIf { it.id == id }
        writeEntities(existing)
    }

    override fun getById(list: String, id: String): Entity.Saved? {
        return getEntities(list).firstOrNull { it.id == id }
    }

    override fun getAllByProperty(
        list: String,
        property: String,
        value: String
    ): List<Entity.Saved> {
        return getEntities(list).filter { entity ->
            entity.properties.any { (first, second) -> first == property && second == value }
        }
    }

    private fun writeEntities(entities: List<Entity.New>) {
        val map = mutableMapOf<String, MutableList<JsonEntity>>()
        entities.forEach {
            map.getOrPut(it.list) { mutableListOf() }.add(it.toJson())
        }

        writeJson(map)
    }

    private fun readEntities(): MutableList<Entity.New> {
        return readJson().entries.flatMap { (list, entities) ->
            entities.map { it.toEntity(list) }
        }.toMutableList()
    }

    private fun readJson(): MutableMap<String, MutableList<JsonEntity>> {
        StrictMode.noteSlowCall("Reading from JSON file")

        if (!entitiesFile.exists()) {
            entitiesFile.parentFile.mkdirs()
            entitiesFile.createNewFile()
        }

        try {
            val typeToken = TypeToken.getParameterized(
                MutableMap::class.java,
                String::class.java,
                TypeToken.getParameterized(MutableList::class.java, JsonEntity::class.java).type
            )

            val json = entitiesFile.readText()
            return if (json.isNotBlank()) {
                val parsedJson =
                    Gson().fromJson<MutableMap<String, MutableList<JsonEntity>>>(
                        json,
                        typeToken.type
                    )

                parsedJson
            } else {
                mutableMapOf()
            }
        } catch (e: Exception) {
            return mutableMapOf()
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
        val version: Int,
        val properties: Map<String, String>,
        val offline: Boolean,
        val trunkVersion: Int?
    )

    private fun JsonEntity.toEntity(list: String): Entity.New {
        val state = if (this.offline) {
            Entity.State.OFFLINE
        } else {
            Entity.State.ONLINE
        }

        return Entity.New(
            list,
            this.id,
            this.label,
            this.version,
            this.properties.entries.map { Pair(it.key, it.value) },
            state,
            this.trunkVersion
        )
    }

    private fun Entity.toJson(): JsonEntity {
        return JsonEntity(
            this.id,
            this.label,
            this.version,
            this.properties.toMap(),
            this.state == Entity.State.OFFLINE,
            this.trunkVersion
        )
    }
}

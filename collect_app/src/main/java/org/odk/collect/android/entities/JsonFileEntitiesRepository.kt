package org.odk.collect.android.entities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.Entity
import java.io.File

class JsonFileEntitiesRepository(directory: File) : EntitiesRepository {

    private val entitiesFile = File(directory, "entities.json")

    override fun getDatasets(): Set<String> {
        return getEntities().map { it.dataset }.toSet()
    }

    override fun getEntities(dataset: String): List<Entity> {
        return getEntities().filter { it.dataset == dataset }
    }

    override fun save(entity: Entity) {
        val updatedEntities = getEntities() + entity
        val json = Gson().toJson(updatedEntities)
        entitiesFile.writeText(json)
    }

    private fun getEntities(): MutableList<Entity> {
        if (!entitiesFile.exists()) {
            entitiesFile.parentFile.mkdirs()
            entitiesFile.createNewFile()
        }

        val typeToken = TypeToken.getParameterized(MutableList::class.java, Entity::class.java)
        return Gson().fromJson(entitiesFile.readText(), typeToken.type)
            ?: mutableListOf()
    }
}

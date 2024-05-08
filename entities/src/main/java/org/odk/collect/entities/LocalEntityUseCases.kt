package org.odk.collect.entities

import org.javarosa.core.model.instance.CsvExternalInstance
import java.io.File

object LocalEntityUseCases {

    fun updateLocalEntities(
        dataset: String,
        onlineList: File,
        entitiesRepository: EntitiesRepository
    ) {
        val root = try {
            CsvExternalInstance().parse(dataset, onlineList.absolutePath)
        } catch (e: Exception) {
            return
        }

        val localEntities = entitiesRepository.getEntities(dataset).associateBy { it.id }
        val listItems = root.getChildrenWithName("item")

        val newAndUpdated = listItems.fold(arrayOf<Entity>()) { entities, item ->
            val id = item.getFirstChild(EntityItemElement.ID)?.value?.value as? String
            val label = item.getFirstChild(EntityItemElement.LABEL)?.value?.value as? String
            val version =
                (item.getFirstChild(EntityItemElement.VERSION)?.value?.value as? String)?.toInt()
            if (id == null || label == null || version == null) {
                return
            }

            val existing = localEntities[id]
            if (existing == null || existing.version < version) {
                val properties = 0.until(item.numChildren)
                    .fold(emptyList<Pair<String, String>>()) { properties, index ->
                        val child = item.getChildAt(index)

                        if (!listOf(
                                EntityItemElement.ID,
                                EntityItemElement.LABEL,
                                EntityItemElement.VERSION
                            ).contains(child.name)
                        ) {
                            properties + Pair(child.name, child.value!!.value as String)
                        } else {
                            properties
                        }
                    }

                entities + Entity(dataset, id, label, version, properties)
            } else {
                entities
            }
        }

        if (newAndUpdated.isNotEmpty()) {
            entitiesRepository.save(*newAndUpdated)
        }
    }
}

package org.odk.collect.entities

import org.javarosa.core.model.instance.CsvExternalInstance
import org.javarosa.core.model.instance.TreeElement
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

        val localEntities = entitiesRepository.getEntities(dataset)
        val serverEntities = root.getChildrenWithName("item")

        val accumulator =
            Pair(arrayOf<Entity>(), localEntities.associateBy { it.id }.toMutableMap())
        val (newAndUpdated, missingFromServer) = serverEntities.fold(accumulator) { (new, missing), item ->
            val entity = parseEntityFromItem(item, dataset) ?: return
            val existing = missing.remove(entity.id)

            if (existing == null || existing.version < entity.version) {
                Pair(new + entity, missing)
            } else if (existing.offline) {
                Pair(new + existing.copy(offline = false), missing)
            } else {
                Pair(new, missing)
            }
        }

        missingFromServer.values.forEach {
            if (!it.offline) {
                entitiesRepository.delete(it.id)
            }
        }

        entitiesRepository.save(*newAndUpdated)
    }

    private fun parseEntityFromItem(
        item: TreeElement,
        dataset: String
    ): Entity? {
        val id = item.getFirstChild(EntityItemElement.ID)?.value?.value as? String
        val label = item.getFirstChild(EntityItemElement.LABEL)?.value?.value as? String
        val version =
            (item.getFirstChild(EntityItemElement.VERSION)?.value?.value as? String)?.toInt()
        if (id == null || label == null || version == null) {
            return null
        }

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

        val entity = Entity(dataset, id, label, version, properties, offline = false)
        return entity
    }
}

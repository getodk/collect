package org.odk.collect.entities.javarosa.intance

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.odk.collect.entities.javarosa.parse.EntityItemElement
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity

class LocalEntitiesInstanceAdapter(private val entitiesRepository: EntitiesRepository) {

    private val lists = entitiesRepository.getLists()

    fun supportsInstance(instanceId: String): Boolean {
        return lists.contains(instanceId)
    }

    fun getAll(instanceId: String, partial: Boolean): List<TreeElement> {
        return entitiesRepository.getEntities(instanceId).mapIndexed { index, entity ->
            if (partial && index == 0) {
                convertToElement(entity, true)
            } else if (partial) {
                TreeElement("item", entity.index, true)
            } else {
                convertToElement(entity, false)
            }
        }
    }

    fun queryEq(instanceId: String, child: String, value: String): List<TreeElement>? {
        return when {
            child == "name" -> {
                val entity = entitiesRepository.getById(
                    instanceId,
                    value
                )

                if (entity != null) {
                    listOf(convertToElement(entity, false))
                } else {
                    emptyList()
                }
            }

            !listOf(EntityItemElement.LABEL, EntityItemElement.VERSION).contains(child) -> {
                val entities = entitiesRepository.getAllByProperty(
                    instanceId,
                    child,
                    value
                )

                entities.map { convertToElement(it, false) }
            }

            else -> null
        }
    }

    private fun convertToElement(entity: Entity.Saved, partial: Boolean): TreeElement {
        val name = TreeElement(EntityItemElement.ID)
        val label = TreeElement(EntityItemElement.LABEL)
        val version = TreeElement(EntityItemElement.VERSION)

        if (!partial) {
            name.value = StringData(entity.id)
            label.value = StringData(entity.label)
            version.value = StringData(entity.version.toString())
        }

        val item = TreeElement("item", entity.index, partial)
        item.addChild(name)
        item.addChild(label)
        item.addChild(version)

        entity.properties.forEach { property ->
            val propertyElement = TreeElement(property.first)

            if (!partial) {
                propertyElement.value = StringData(property.second)
            }

            item.addChild(propertyElement)
        }

        return item
    }
}

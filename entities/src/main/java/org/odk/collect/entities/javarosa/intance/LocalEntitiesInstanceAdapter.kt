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
        return if (partial) {
            val count = entitiesRepository.getCount(instanceId)

            if (count > 0) {
                val first = entitiesRepository.getByIndex(instanceId, 0)!!

                0.until(count).map {
                    if (it == 0) {
                        convertToElement(first, true)
                    } else {
                        TreeElement("item", it, true)
                    }
                }
            } else {
                emptyList()
            }
        } else {
            entitiesRepository.getEntities(instanceId).map { entity ->
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
        val trunkVersion = TreeElement(EntityItemElement.TRUNK_VERSION)
        val branchId = TreeElement(EntityItemElement.BRANCH_ID)

        if (!partial) {
            name.value = StringData(entity.id)
            version.value = StringData(entity.version.toString())
            branchId.value = StringData(entity.branchId)

            if (entity.label != null) {
                label.value = StringData(entity.label)
            }

            if (entity.trunkVersion != null) {
                trunkVersion.value = StringData(entity.trunkVersion.toString())
            }
        }

        val item = TreeElement("item", entity.index, partial)
        item.addChild(name)
        item.addChild(label)
        item.addChild(version)
        item.addChild(trunkVersion)
        item.addChild(branchId)

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

package org.odk.collect.entities.javarosa.intance

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeElement
import org.odk.collect.entities.browser.EntityItemElement
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.entities.storage.Entity

class LocalEntitiesInstanceAdapter(private val entitiesRepository: EntitiesRepository) {

    private val lists = entitiesRepository.getLists()

    fun supportsInstance(sourceInstance: DataInstance<*>): Boolean {
        return lists.contains(sourceInstance.instanceId)
    }

    fun queryEq(sourceInstance: DataInstance<*>, child: String, value: String): List<TreeElement>? {
        return when {
            child == "name" -> {
                val entity = entitiesRepository.getById(
                    sourceInstance.instanceId,
                    value
                )

                if (entity != null) {
                    val element = convertToElement(sourceInstance, entity)
                    listOf(element)
                } else {
                    emptyList()
                }
            }

            !listOf("label", "__version").contains(child) -> {
                val entities = entitiesRepository.getAllByProperty(
                    sourceInstance.instanceId,
                    child,
                    value
                )

                entities.map { convertToElement(sourceInstance, it) }
            }

            else -> null
        }
    }

    private fun convertToElement(
        sourceInstance: DataInstance<*>,
        entity: Entity.Saved
    ): TreeElement {
        val name = TreeElement(EntityItemElement.ID)
        val label = TreeElement(EntityItemElement.LABEL)
        val version = TreeElement(EntityItemElement.VERSION)

        name.value = StringData(entity.id)
        label.value = StringData(entity.label)
        version.value = StringData(entity.version.toString())

        val item = TreeElement("item", entity.index)
        item.addChild(name)
        item.addChild(label)
        item.addChild(version)

        entity.properties.forEach { property ->
            val propertyElement = TreeElement(property.first)
            propertyElement.value = StringData(property.second)
            item.addChild(propertyElement)
        }

        item.parent = sourceInstance.root
        return item
    }
}

package org.odk.collect.entities.javarosa.intance

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.xform.parse.ExternalInstanceParser
import org.odk.collect.entities.browser.EntityItemElement
import org.odk.collect.entities.storage.EntitiesRepository

internal class LocalEntitiesFileInstanceParser(private val entitiesRepositoryProvider: () -> EntitiesRepository) :
    ExternalInstanceParser.FileInstanceParser {

    override fun parse(instanceId: String, path: String): TreeElement {
        return parse(instanceId, path, false)
    }

    override fun parse(instanceId: String, path: String, partial: Boolean): TreeElement {
        val root = TreeElement("root", 0)

        val entitiesRepository = entitiesRepositoryProvider()
        entitiesRepository.getEntities(instanceId).forEachIndexed { index, entity ->
            val name = TreeElement(EntityItemElement.ID)
            val label = TreeElement(EntityItemElement.LABEL)
            val version = TreeElement(EntityItemElement.VERSION)

            if (!partial) {
                name.value = StringData(entity.id)
                label.value = StringData(entity.label)
                version.value = StringData(entity.version.toString())
            }

            val item = TreeElement("item", index, partial)
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

            root.addChild(item)
        }

        return root
    }

    override fun isSupported(instanceId: String, instanceSrc: String): Boolean {
        val entitiesRepository = entitiesRepositoryProvider()
        return entitiesRepository.getLists().contains(instanceId)
    }
}

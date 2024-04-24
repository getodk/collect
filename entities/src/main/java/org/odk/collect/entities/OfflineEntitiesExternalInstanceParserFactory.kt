package org.odk.collect.entities

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.reference.ReferenceManager
import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.parse.ExternalInstanceParserFactory

class OfflineEntitiesExternalInstanceParserFactory(
    private val entitiesRepositoryProvider: () -> EntitiesRepository,
    private val enabled: () -> Boolean
) : ExternalInstanceParserFactory {
    override fun getExternalInstanceParser(): ExternalInstanceParser {
        return if (enabled()) {
            OfflineEntitiesExternalInstanceParser(entitiesRepositoryProvider())
        } else {
            ExternalInstanceParser()
        }
    }
}

internal class OfflineEntitiesExternalInstanceParser(private val entitiesRepository: EntitiesRepository) :
    ExternalInstanceParser() {

    override fun parse(
        referenceManager: ReferenceManager,
        instanceId: String,
        instanceSrc: String
    ): TreeElement {
        if (entitiesRepository.getDatasets().contains(instanceId)) {
            val root = TreeElement("root", 0)

            entitiesRepository.getEntities(instanceId).forEachIndexed { index, entity ->
                val name = TreeElement(EntityItemElement.ID)
                name.value = StringData(entity.id)

                val label = TreeElement(EntityItemElement.LABEL)
                label.value = StringData(entity.label)

                val item = TreeElement("item", index)
                item.addChild(name)
                item.addChild(label)

                entity.properties.forEach { property ->
                    addChild(item, property)
                }

                root.addChild(item)
            }

            return root
        } else {
            return super.parse(referenceManager, instanceId, instanceSrc)
        }
    }

    private fun addChild(
        element: TreeElement,
        nameAndValue: Pair<String, String>
    ) {
        element.addChild(
            TreeElement(nameAndValue.first).also {
                it.value = StringData(nameAndValue.second)
            }
        )
    }
}

package org.odk.collect.entities

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.parse.ExternalInstanceParserFactory

class OfflineEntitiesExternalInstanceParserFactory(
    private val entitiesRepositoryProvider: () -> EntitiesRepository,
    private val enabled: () -> Boolean
) : ExternalInstanceParserFactory {
    override fun getExternalInstanceParser(): ExternalInstanceParser {
        val parser = ExternalInstanceParser()

        if (enabled()) {
            parser.addProcessor(
                OfflineEntitiesExternalDataInstanceProcessor(
                    entitiesRepositoryProvider()
                )
            )
        }

        return parser
    }
}

internal class OfflineEntitiesExternalDataInstanceProcessor(private val entitiesRepository: EntitiesRepository) :
    ExternalInstanceParser.ExternalDataInstanceProcessor {
    override fun processInstance(id: String, root: TreeElement) {
        if (entitiesRepository.getDatasets().contains(id)) {
            0.until(root.numChildren).forEach { root.removeChildAt(it) }

            entitiesRepository.getEntities(id).forEachIndexed { index, entity ->
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

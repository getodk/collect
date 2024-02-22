package org.odk.collect.entities

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser

class OfflineEntitiesXFormParserFactory(
    base: IXFormParserFactory,
    private val entitiesRepositoryProvider: () -> EntitiesRepository
) : IXFormParserFactory.Wrapper(base) {
    override fun apply(xFormParser: XFormParser): XFormParser {
        xFormParser.addProcessor(
            OfflineEntitiesExternalDataInstanceProcessor(
                entitiesRepositoryProvider()
            )
        )

        return xFormParser
    }
}

internal class OfflineEntitiesExternalDataInstanceProcessor(private val entitiesRepository: EntitiesRepository) :
    XFormParser.ExternalDataInstanceProcessor {
    override fun processInstance(id: String, root: TreeElement) {
        if (entitiesRepository.getDatasets().contains(id)) {
            val items = root.getChildrenWithName(ITEM_ELEMENT)
            val startingMultiplicity = items.size

            entitiesRepository.getEntities(id).forEachIndexed { index, entity ->
                val duplicateIndex =
                    items.indexOfFirst { it.getFirstChild(ID_ELEMENT)?.value?.value == entity.id }

                if (duplicateIndex == -1) {
                    val name = TreeElement(ID_ELEMENT)
                    name.value = StringData(entity.id)

                    val label = TreeElement(LABEL_ELEMENT)
                    label.value = StringData(entity.label)

                    val item = TreeElement(ITEM_ELEMENT, startingMultiplicity + index)
                    item.addChild(name)
                    item.addChild(label)

                    root.addChild(item)
                } else {
                    val duplicateElement = root.getChildAt(duplicateIndex)
                    duplicateElement.getFirstChild(LABEL_ELEMENT)!!.value = StringData(entity.label)
                    entity.properties.forEach { property ->
                        val propertyElement = duplicateElement.getFirstChild(property.first)
                        if (propertyElement != null) {
                            propertyElement.value = StringData(property.second)
                        } else {
                            duplicateElement.addChild(
                                TreeElement(property.first).also {
                                    it.value = StringData(property.second)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ID_ELEMENT = "name"
        private const val LABEL_ELEMENT = "label"
        private const val ITEM_ELEMENT = "item"
    }
}

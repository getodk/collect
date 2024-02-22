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

private class OfflineEntitiesExternalDataInstanceProcessor(private val entitiesRepository: EntitiesRepository) :
    XFormParser.ExternalDataInstanceProcessor {
    override fun processInstance(id: String, root: TreeElement) {
        if (entitiesRepository.getDatasets().contains(id)) {
            val startingMultiplicity = root.numChildren

            entitiesRepository.getEntities(id).forEachIndexed { index, entity ->
                val name = TreeElement("name")
                name.value = StringData(entity.id)

                val label = TreeElement("label")
                label.value = StringData(entity.label)

                val item = TreeElement("item", startingMultiplicity + index)
                item.addChild(name)
                item.addChild(label)

                root.addChild(item)
            }
        }
    }
}

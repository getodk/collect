package org.odk.collect.entities

import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.xform.parse.ExternalInstanceParser
import org.javarosa.xform.parse.ExternalInstanceParser.FileInstanceParser
import org.javarosa.xform.parse.ExternalInstanceParserFactory

class LocalEntitiesExternalInstanceParserFactory(
    private val entitiesRepositoryProvider: () -> EntitiesRepository,
    private val enabled: () -> Boolean
) : ExternalInstanceParserFactory {
    override fun getExternalInstanceParser(): ExternalInstanceParser {
        val parser = ExternalInstanceParser()

        if (enabled()) {
            parser.addFileInstanceParser(LocalEntitiesFileInstanceParser(entitiesRepositoryProvider))
        }

        return parser
    }
}

internal class LocalEntitiesFileInstanceParser(private val entitiesRepositoryProvider: () -> EntitiesRepository) :
    FileInstanceParser {

    override fun parse(instanceId: String, path: String): TreeElement {
        val root = TreeElement("root", 0)

        val entitiesRepository = entitiesRepositoryProvider()
        entitiesRepository.getEntities(instanceId).forEachIndexed { index, entity ->
            val name = TreeElement(EntityItemElement.ID)
            name.value = StringData(entity.id)

            val label = TreeElement(EntityItemElement.LABEL)
            label.value = StringData(entity.label)

            val version = TreeElement(EntityItemElement.VERSION)
            version.value = StringData(entity.version.toString())

            val item = TreeElement("item", index)
            item.addChild(name)
            item.addChild(label)
            item.addChild(version)

            entity.properties.forEach { property ->
                addChild(item, property)
            }

            root.addChild(item)
        }

        return root
    }

    override fun isSupported(instanceId: String, instanceSrc: String): Boolean {
        val entitiesRepository = entitiesRepositoryProvider()
        return entitiesRepository.getDatasets().contains(instanceId)
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

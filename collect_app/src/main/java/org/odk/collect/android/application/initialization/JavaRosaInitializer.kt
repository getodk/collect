package org.odk.collect.android.application.initialization

import org.javarosa.core.model.CoreModelModule
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.services.PrototypeManager
import org.javarosa.core.util.JavaRosaCoreModule
import org.javarosa.entities.EntityXFormParserFactory
import org.javarosa.model.xform.XFormsModule
import org.javarosa.xform.parse.IXFormParserFactory
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParser.ExternalDataInstanceProcessor
import org.javarosa.xform.parse.XFormParserFactory
import org.javarosa.xform.util.XFormUtils
import org.odk.collect.android.dynamicpreload.DynamicPreloadXFormParserFactory
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.metadata.PropertyManager

class JavaRosaInitializer(
    private val propertyManager: PropertyManager,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider
) {

    fun initialize() {
        propertyManager.reload()
        org.javarosa.core.services.PropertyManager
            .setPropertyManager(propertyManager)

        // Register prototypes for classes that FormDef uses
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames)
        PrototypeManager.registerPrototypes(CoreModelModule.classNames)
        XFormsModule().registerModule()

        // When registering prototypes from Collect, a proguard exception also needs to be added
        PrototypeManager.registerPrototype("org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction")
        XFormParser.registerActionHandler(
            CollectSetGeopointActionHandler.ELEMENT_NAME,
            CollectSetGeopointActionHandler()
        )

        // Configure default parser factory
        val entityXFormParserFactory = EntityXFormParserFactory(XFormParserFactory())
        val dynamicPreloadXFormParserFactory =
            DynamicPreloadXFormParserFactory(entityXFormParserFactory)
        val offlineEntitiesXFormParserFactory = OfflineEntitiesXFormParserFactory(
            dynamicPreloadXFormParserFactory,
            entitiesRepositoryProvider
        )

        XFormUtils.setXFormParserFactory(offlineEntitiesXFormParserFactory)
    }
}

private class OfflineEntitiesExternalDataInstanceProcessor(private val entitiesRepository: EntitiesRepository) :
    ExternalDataInstanceProcessor {
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

private class OfflineEntitiesXFormParserFactory(
    base: IXFormParserFactory,
    private val entitiesRepositoryProvider: EntitiesRepositoryProvider
) : IXFormParserFactory.Wrapper(base) {
    override fun apply(xFormParser: XFormParser): XFormParser {
        xFormParser.addProcessor(
            OfflineEntitiesExternalDataInstanceProcessor(
                entitiesRepositoryProvider.get()
            )
        )
        return xFormParser
    }
}

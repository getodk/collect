package org.odk.collect.android.formmanagement

import org.javarosa.core.model.FormDef
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.application.Collect
import org.odk.collect.android.dynamicpreload.ExternalDataManagerImpl
import org.odk.collect.android.dynamicpreload.handler.ExternalDataHandlerPull
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory
import org.odk.collect.entities.javarosa.filter.LocalEntitiesFilterStrategy
import org.odk.collect.entities.javarosa.filter.PullDataFunctionHandler
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor
import org.odk.collect.entities.storage.EntitiesRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import java.io.File

class CollectFormEntryControllerFactory(
    private val entitiesRepository: EntitiesRepository,
    private val settings: Settings
) :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef, formMediaDir: File): FormEntryController {
        val externalDataManager = ExternalDataManagerImpl(formMediaDir).also {
            Collect.getInstance().externalDataManager = it
        }

        return FormEntryController(FormEntryModel(formDef)).also {
            val externalDataHandlerPull = ExternalDataHandlerPull(externalDataManager)
            it.addFunctionHandler(PullDataFunctionHandler(entitiesRepository, externalDataHandlerPull))
            it.addPostProcessor(EntityFormFinalizationProcessor())

            if (settings.getBoolean(ProjectKeys.KEY_LOCAL_ENTITIES)) {
                it.addFilterStrategy(LocalEntitiesFilterStrategy(entitiesRepository))
            }
        }
    }
}

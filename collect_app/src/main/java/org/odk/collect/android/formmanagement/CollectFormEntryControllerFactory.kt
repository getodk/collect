package org.odk.collect.android.formmanagement

import org.javarosa.core.model.FormDef
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.application.Collect
import org.odk.collect.android.dynamicpreload.ExternalDataManagerImpl
import org.odk.collect.android.dynamicpreload.handler.ExternalDataHandlerPull
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory
import org.odk.collect.entities.javarosa.filter.LocalEntitiesFilterStrategy
import org.odk.collect.entities.javarosa.finalization.EntityFormFinalizationProcessor
import org.odk.collect.entities.storage.EntitiesRepository
import java.io.File

class CollectFormEntryControllerFactory(val entitiesRepository: EntitiesRepository) :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef, formMediaDir: File): FormEntryController {
        val externalDataManager = ExternalDataManagerImpl(formMediaDir).also {
            Collect.getInstance().externalDataManager = it
        }

        return FormEntryController(FormEntryModel(formDef)).also {
            it.addFunctionHandler(ExternalDataHandlerPull(externalDataManager))
            it.addFilterStrategy(LocalEntitiesFilterStrategy(entitiesRepository))
            it.addPostProcessor(EntityFormFinalizationProcessor())
        }
    }
}

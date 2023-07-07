package org.odk.collect.android.formmanagement

import org.javarosa.core.model.FormDef
import org.javarosa.entities.EntityFormFinalizationProcessor
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.application.Collect
import org.odk.collect.android.dynamicpreload.ExternalDataManagerImpl
import org.odk.collect.android.dynamicpreload.handler.ExternalDataHandlerPull
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory
import java.io.File

class CollectFormEntryControllerFactory :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef, formMediaDir: File): FormEntryController {
        val externalDataManager = ExternalDataManagerImpl(formMediaDir).also {
            Collect.getInstance().externalDataManager = it
        }

        val externalDataHandlerPull = ExternalDataHandlerPull(externalDataManager)
        formDef.evaluationContext.addFunctionHandler(externalDataHandlerPull)

        return FormEntryController(FormEntryModel(formDef)).also {
            it.addPostProcessor(EntityFormFinalizationProcessor())
        }
    }
}

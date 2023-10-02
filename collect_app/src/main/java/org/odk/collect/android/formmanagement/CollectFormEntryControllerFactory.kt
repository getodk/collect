package org.odk.collect.android.formmanagement

import org.javarosa.core.model.FormDef
import org.javarosa.entities.EntityFormFinalizationProcessor
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.tasks.FormLoaderTask.FormEntryControllerFactory

class CollectFormEntryControllerFactory :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef): FormEntryController {
        return FormEntryController(FormEntryModel(formDef)).also {
            it.addPostProcessor(EntityFormFinalizationProcessor())
        }
    }
}

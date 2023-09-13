package org.odk.collect.android.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.instance.InstanceInitializationFactory
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.xform.util.XFormUtils
import org.junit.Test
import org.odk.collect.android.entities.InMemEntitiesRepository
import org.odk.collect.android.javarosawrapper.JavaRosaFormController
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.shared.TempFiles
import java.io.File

class FormEntryUseCasesTest {

    @Test
    fun finalizeDraft_whenValidationFails_marksInstanceAsHavingErrors() {
        val formMediaDir = TempFiles.createTempDir()
        val instanceFile = TempFiles.createTempFile("instance", ".xml")
        val instancesRepository = InMemInstancesRepository()

        val xForm = copyTestForm("forms/two-question-required.xml")
        val formDef = XFormUtils.getFormFromFormXml(xForm.absolutePath, null)
        val newFormController = FormEntryUseCases.loadBlankForm(
            FormEntryController(FormEntryModel(formDef)),
            formMediaDir,
            instanceFile
        )
        val instance =
            FormEntryUseCases.saveDraft(newFormController, instancesRepository, instanceFile)

        val draftController = FormEntryUseCases.loadDraft(
            FormEntryController(FormEntryModel(formDef)),
            formMediaDir,
            instanceFile
        )

        FormEntryUseCases.finalizeDraft(
            draftController,
            instancesRepository,
            InMemEntitiesRepository()
        )

        assertThat(
            instancesRepository.get(instance.dbId)!!.status,
            equalTo(Instance.STATUS_INVALID)
        )
    }

    private fun copyTestForm(testForm: String): File {
        return TempFiles.createTempFile().also {
            FileUtils.copyFileFromResources(testForm, it.absolutePath)
        }
    }
}

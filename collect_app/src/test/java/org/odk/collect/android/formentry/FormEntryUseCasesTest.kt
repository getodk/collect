package org.odk.collect.android.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.data.IntegerData
import org.javarosa.core.reference.ReferenceManager
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.model.xform.XFormsModule
import org.javarosa.xform.util.XFormUtils
import org.junit.Before
import org.junit.Test
import org.kxml2.io.KXmlParser
import org.kxml2.kdom.Document
import org.odk.collect.android.entities.InMemEntitiesRepository
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormUtils
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.shared.TempFiles
import java.io.File
import java.io.StringReader

class FormEntryUseCasesTest {

    private val projectRootDir = TempFiles.createTempDir()
    private val instancesRepository = InMemInstancesRepository()

    @Before
    fun setup() {
        XFormsModule().registerModule()
    }

    @Test
    fun finalizeDraft_whenValidationFails_marksInstanceAsHavingErrors() {
        val formMediaDir = TempFiles.createTempDir()
        val xForm = copyTestForm("forms/two-question-required.xml")
        val formDef = parseForm(xForm, projectRootDir, formMediaDir)
        val instance = createDraft(formDef, formMediaDir, instancesRepository)

        val draftController = FormEntryUseCases.loadDraft(
            FormEntryController(FormEntryModel(formDef)),
            formMediaDir,
            File(instance.instanceFilePath)
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

    @Test
    fun finalizeDraft_canCreatePartialSubmissions() {
        val formMediaDir = TempFiles.createTempDir()
        val xForm = copyTestForm("forms/one-question-partial.xml")
        val formDef = parseForm(xForm, projectRootDir, formMediaDir)
        val instance = createDraft(formDef, formMediaDir, instancesRepository) {
            it.stepToNextScreenEvent()
            it.answerQuestion(it.getFormIndex(), IntegerData(64))
        }

        val draftController = FormEntryUseCases.loadDraft(
            FormEntryController(FormEntryModel(formDef)),
            formMediaDir,
            File(instance.instanceFilePath)
        )

        FormEntryUseCases.finalizeDraft(
            draftController,
            instancesRepository,
            InMemEntitiesRepository()
        )

        val updatedInstance = instancesRepository.get(instance.dbId)!!
        assertThat(updatedInstance.canEditWhenComplete(), equalTo(false))

        val root = parseXml(File(updatedInstance.instanceFilePath)).rootElement
        assertThat(root.name, equalTo("age"))
        assertThat(root.childCount, equalTo(1))
        assertThat(root.getChild(0), equalTo("64"))
    }

    private fun parseForm(xForm: File, projectRootDir: File, formMediaDir: File): FormDef {
        FormUtils.setupReferenceManagerForForm(ReferenceManager.instance(), projectRootDir, formMediaDir)
        return XFormUtils.getFormFromFormXml(xForm.absolutePath, null)
    }

    private fun createDraft(
        formDef: FormDef,
        formMediaDir: File,
        instancesRepository: InMemInstancesRepository,
        fillIn: (FormController) -> Any = {}
    ): Instance {
        val instanceFile = TempFiles.createTempFile("instance", ".xml")

        val formController = FormEntryUseCases.loadBlankForm(
            FormEntryController(FormEntryModel(formDef)),
            formMediaDir,
            instanceFile
        )

        fillIn(formController)
        return FormEntryUseCases.saveDraft(formController, instancesRepository, instanceFile)
    }

    private fun copyTestForm(testForm: String): File {
        return TempFiles.createTempFile().also {
            FileUtils.copyFileFromResources(testForm, it.absolutePath)
        }
    }

    private fun parseXml(file: File): Document {
        return StringReader(String(file.readBytes())).use { reader ->
            val parser = KXmlParser()
            parser.setInput(reader)
            Document().also { it.parse(parser) }
        }
    }
}

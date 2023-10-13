package org.odk.collect.android.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.data.IntegerData
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.model.xform.XFormsModule
import org.junit.Before
import org.junit.Test
import org.kxml2.io.KXmlParser
import org.kxml2.kdom.Document
import org.mockito.kotlin.mock
import org.odk.collect.android.entities.InMemEntitiesRepository
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.shared.TempFiles
import java.io.File
import java.io.StringReader

class FormEntryUseCasesTest {

    private val projectRootDir = TempFiles.createTempDir()
    private val formsRepository = InMemFormsRepository()
    private val instancesRepository = InMemInstancesRepository()

    @Before
    fun setup() {
        XFormsModule().registerModule()
    }

    @Test
    fun loadFormDef_withInstance_loadsCorrectFormVersion() {
        createForm(
            copyTestForm("forms/one-question.xml"),
            formId = "one-question",
            version = "1"
        )

        val (formUpdated, formDefUpdated) = createForm(
            copyTestForm("forms/one-question-updated.xml"),
            formId = "one-question",
            version = "2"
        )

        val instance = createDraft(formUpdated, formDefUpdated, instancesRepository)

        val loadedFormDef =
            FormEntryUseCases.loadFormDef(instance, formsRepository, projectRootDir, mock()).first
        assertThat(loadedFormDef.title, equalTo("One Question Updated"))
    }

    @Test
    fun finalizeDraft_whenValidationFails_marksInstanceAsHavingErrors() {
        val (form, formDef) = createForm(copyTestForm("forms/two-question-required.xml"))
        val instance = createDraft(form, formDef, instancesRepository)

        val draftController = FormEntryUseCases.loadDraft(
            form,
            instance,
            FormEntryController(FormEntryModel(formDef))
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
        val (form, formDef) = createForm(copyTestForm("forms/one-question-partial.xml"))
        val instance = createDraft(form, formDef, instancesRepository) {
            it.stepToNextScreenEvent()
            it.answerQuestion(it.getFormIndex(), IntegerData(64))
        }

        val draftController = FormEntryUseCases.loadDraft(
            form,
            instance,
            FormEntryController(FormEntryModel(formDef))
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

    @Test
    fun finalizeDraft_updatesInstanceNameInRepository() {
        val (form, formDef) = createForm(copyTestForm("forms/one-question-uuid-instance-name.xml"))
        val instance = createDraft(form, formDef, instancesRepository)

        val draftController = FormEntryUseCases.loadDraft(
            form,
            instance,
            FormEntryController(FormEntryModel(formDef))
        )

        FormEntryUseCases.finalizeDraft(
            draftController,
            instancesRepository,
            InMemEntitiesRepository()
        )

        val updatedInstance = instancesRepository.get(instance.dbId)!!
        assertThat(
            updatedInstance.displayName,
            equalTo(draftController.getSubmissionMetadata()!!.instanceName)
        )
    }

    private fun createForm(
        xForm: File,
        formId: String = "formId",
        version: String = "formVersion"
    ): Pair<Form, FormDef> {
        val form = formsRepository.save(
            FormFixtures.form(
                formId = formId,
                version = version,
                formFilePath = xForm.absolutePath
            )
        )
        val formDef = FormEntryUseCases.loadFormDef(form, projectRootDir, mock())
        return Pair(form, formDef)
    }

    private fun createDraft(
        form: Form,
        formDef: FormDef,
        instancesRepository: InMemInstancesRepository,
        fillIn: (FormController) -> Any = {}
    ): Instance {
        val instanceFile = TempFiles.createTempFile("instance", ".xml")

        val formController = FormEntryUseCases.loadBlankForm(
            form,
            FormEntryController(FormEntryModel(formDef)),
            instanceFile
        )

        fillIn(formController)
        val instance = FormEntryUseCases.saveDraft(form, formController, instancesRepository)
        return instance
    }

    private fun copyTestForm(testForm: String): File {
        return TempFiles.createTempFile(".xml").also {
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

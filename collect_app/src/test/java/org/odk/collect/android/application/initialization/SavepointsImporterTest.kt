package org.odk.collect.android.application.initialization

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.formstest.FormFixtures
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.projects.Project
import org.odk.collect.shared.TimeInMs
import org.odk.collect.shared.strings.RandomString
import java.io.File

@RunWith(AndroidJUnit4::class)
class SavepointsImporterTest {
    private val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)

    private val projectsRepository = component.projectsRepository()
    private val projectDependencyModuleFactory = component.projectDependencyModuleFactory()

    private val savepointsImporter =
        SavepointsImporter(projectsRepository, projectDependencyModuleFactory)

    private val project = projectsRepository.save(Project.DEMO_PROJECT)
    private val projectDependencyModule = projectDependencyModuleFactory.create(project.uuid)
    private val savepointsRepository = projectDependencyModule.savepointsRepository
    private val storagePathProvider = component.storagePathProvider()
    private val formsRepository = projectDependencyModule.formsRepository
    private val instancesRepository = projectDependencyModule.instancesRepository

    @Test
    fun ifABlankFormHasNoSavepoint_nothingShouldBeImported() {
        // create blank forms
        createBlankForm(project, "sampleForm", "1")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifABlankFormHasASavepointCreatedEarlierThanTheForm_nothingShouldBeImported() {
        // create blank forms
        createBlankForm(project, "sampleForm", "1", date = System.currentTimeMillis() + TimeInMs.ONE_HOUR)

        // create savepoints
        createFileInCache(project, "sampleForm_2024-04-10_01-35-41.xml.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifABlankFormHasASavepointButTheFormIsSoftDeleted_nothingShouldBeImported() {
        // create blank forms
        createBlankForm(project, "sampleForm", "1", deleted = true)

        // create savepoints
        createFileInCache(project, "sampleForm_2024-04-10_01-35-41.xml.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifAFileForABlankFormExistsWithMatchingName_butIncorrectSuffix_nothingShouldBeImported() {
        val formName = "sampleForm"

        // create blank forms
        createBlankForm(project, formName, "1", "1")

        // create savepoints
        createFileInCache(project, "${formName}_2024-04-10_01-35-41.xml")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifThereAreMultipleDifferentBlankFormsWithSavepoints_allSavepointsShouldBeImported() {
        val form1Name = "sampleForm1"
        val form2Name = "sampleForm2"

        // create blank forms
        val blankForm1 = createBlankForm(project, form1Name, "1")
        val blankForm2 = createBlankForm(project, form2Name, "2")

        // create savepoints
        val savepointFile1 = createFileInCache(project, "${form1Name}_2024-04-10_01-35-41.xml.save")
        val savepointFile2 = createFileInCache(project, "${form2Name}_2024-04-10_01-35-42.xml.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        val expectedSavepoint1 =
            Savepoint(blankForm1.dbId, null, savepointFile1.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/${form1Name}_2024-04-10_01-35-41/${form1Name}_2024-04-10_01-35-41.xml")
        val expectedSavepoint2 =
            Savepoint(blankForm2.dbId, null, savepointFile2.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/${form2Name}_2024-04-10_01-35-42/${form2Name}_2024-04-10_01-35-42.xml")
        assertThat(savepoints, contains(expectedSavepoint1, expectedSavepoint2))
    }

    @Test
    fun formFileNameShouldBeEscapedAndTreatedAsLiteralTextToAvoidPatternSyntaxException() {
        val form1Name = "sampleForm("

        // create blank forms
        val blankForm1 = createBlankForm(project, form1Name, "1")

        // create savepoints
        val savepointFile1 = createFileInCache(project, "${form1Name}_2024-04-10_01-35-41.xml.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        val expectedSavepoint1 =
            Savepoint(blankForm1.dbId, null, savepointFile1.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/${form1Name}_2024-04-10_01-35-41/${form1Name}_2024-04-10_01-35-41.xml")

        assertThat(savepoints, contains(expectedSavepoint1))
    }

    @Test
    fun ifThereAreMultipleVersionsOfTheSameBlankFormWithSavepoints_allSavepointsShouldBeImported() {
        val form1Name = "sampleForm"
        val form2Name = "sampleForm_1"

        // create blank forms
        val blankForm1 = createBlankForm(project, form1Name, "1", "1", date = 1)
        val blankForm2 = createBlankForm(project, form2Name, "1", "2", date = 2)

        // create savepoints
        val savepointFile1 = createFileInCache(project, "${form1Name}_2024-04-10_01-35-41.xml.save")
        val savepointFile2 = createFileInCache(project, "${form2Name}_2024-04-10_01-35-42.xml.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        val expectedSavepoint1 =
            Savepoint(blankForm1.dbId, null, savepointFile1.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/${form1Name}_2024-04-10_01-35-41/${form1Name}_2024-04-10_01-35-41.xml")
        val expectedSavepoint2 =
            Savepoint(blankForm2.dbId, null, savepointFile2.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/${form2Name}_2024-04-10_01-35-42/${form2Name}_2024-04-10_01-35-42.xml")
        assertThat(savepoints, contains(expectedSavepoint1, expectedSavepoint2))
    }

    @Test
    fun ifASavedFormHasNoSavepoint_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1")

        // create saved forms
        createSavedForm("sampleForm", form)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointCreatedEarlierThanTheForm_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1")

        // create saved forms
        val savedForm = createSavedForm("sampleForm", form, lastStatusChangeDate = System.currentTimeMillis() + TimeInMs.ONE_HOUR)

        // create savepoints
        createFileInCache(project, "${File(savedForm.instanceFilePath).name}.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointButItsBlankFormIsSoftDeleted_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1", deleted = true)

        // create saved forms
        val savedForm = createSavedForm("sampleForm", form)

        // create savepoints
        createFileInCache(project, "${File(savedForm.instanceFilePath).name}.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointButItsBlankFormDoesNotExist_nothingShouldBeImported() {
        // create saved forms
        val savedForm = createSavedForm("sampleForm", FormFixtures.form("1"))

        // create savepoints
        createFileInCache(project, "${File(savedForm.instanceFilePath).name}.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointButTheFormIsSoftDeleted_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1")

        // create saved forms
        val savedForm = createSavedForm("sampleForm", form, deletedDate = System.currentTimeMillis())

        // create savepoints
        createFileInCache(project, "${File(savedForm.instanceFilePath).name}.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifAFileForASavedFormExistsWithMatchingName_butIncorrectSuffix_nothingShouldBeImported() {
        val formName = "sampleForm"

        // create blank forms
        val form = createBlankForm(project, formName, "1")

        // create saved forms
        val savedForm = createSavedForm(formName, form)

        // create savepoints
        createFileInCache(project, File(savedForm.instanceFilePath).name)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifThereAreMultipleDifferentSavedFormsWithSavepoints_allSavepointsShouldBeImported() {
        val form1Name = "sampleForm1"
        val form2Name = "sampleForm2"

        // create blank forms
        val form1 = createBlankForm(project, form1Name, "1")
        val form2 = createBlankForm(project, form2Name, "2")

        // create saved forms
        val savedForm1 = createSavedForm(form1Name, form1)
        val savedForm2 = createSavedForm(form2Name, form2)

        // create savepoints
        val savepointFile1 = createFileInCache(project, "${File(savedForm1.instanceFilePath).name}.save")
        val savepointFile2 = createFileInCache(project, "${File(savedForm2.instanceFilePath).name}.save")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepository.getAll()
        val expectedSavepoint1 = Savepoint(1, 1, savepointFile1.absolutePath, savedForm1.instanceFilePath)
        val expectedSavepoint2 = Savepoint(2, 2, savepointFile2.absolutePath, savedForm2.instanceFilePath)
        assertThat(savepoints, contains(expectedSavepoint1, expectedSavepoint2))
    }

    private fun createBlankForm(project: Project.Saved, formName: String, formId: String, formVersion: String = "1", date: Long = 0, deleted: Boolean = false): Form {
        val formFile = File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, project.uuid), "$formName.xml").also {
            it.writeText(RandomString.randomString(10))
        }
        val blankForm = formsRepository.save(FormFixtures.form(formId, formVersion, formFile.absolutePath))
        return formsRepository.save(Form.Builder(blankForm).date(date).deleted(deleted).build())
    }

    private fun createSavedForm(formName: String, form: Form, lastStatusChangeDate: Long = System.currentTimeMillis() - TimeInMs.ONE_HOUR, deletedDate: Long? = null): Instance {
        return instancesRepository.save(InstanceFixtures.instance(displayName = formName, form = form, lastStatusChangeDate = lastStatusChangeDate, deletedDate = deletedDate))
    }

    private fun createFileInCache(project: Project.Saved, fileName: String): File {
        val cacheDir = File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, project.uuid))
        return File(cacheDir, fileName).also {
            it.writeText(RandomString.randomString(10))
        }
    }
}

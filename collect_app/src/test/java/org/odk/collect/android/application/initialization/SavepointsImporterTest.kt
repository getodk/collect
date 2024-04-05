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
    private val instancesRepositoryProvider = component.instancesRepositoryProvider()
    private val formsRepositoryProvider = component.formsRepositoryProvider()
    private val savepointsRepositoryProvider = component.savepointsRepositoryProvider()
    private val storagePathProvider = component.storagePathProvider()

    private val savepointsImporter =
        SavepointsImporter(projectsRepository, instancesRepositoryProvider, formsRepositoryProvider, savepointsRepositoryProvider, storagePathProvider)

    private val project = projectsRepository.save(Project.DEMO_PROJECT)

    @Test
    fun ifABlankFormHasNoSavepoint_nothingShouldBeImported() {
        // create blank forms
        createBlankForm(project, "sampleForm", "1")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifABlankFormHasASavepointCreatedEarlierThanTheForm_nothingShouldBeImported() {
        // create blank forms
        createBlankForm(project, "sampleForm", "1", date = System.currentTimeMillis() + TimeInMs.ONE_HOUR)

        // create savepoints
        createSavepointFile(project, "sampleForm_${System.currentTimeMillis()}.xml")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifABlankFormHasASavepointButTheFormIsSoftDeleted_nothingShouldBeImported() {
        // create blank forms
        createBlankForm(project, "sampleForm", "1", deleted = true)

        // create savepoints
        createSavepointFile(project, "sampleForm_${System.currentTimeMillis()}.xml")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
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
        val savepointFile1 = createSavepointFile(project, "${form1Name}_${System.currentTimeMillis()}.xml")
        val savepointFile2 = createSavepointFile(project, "${form2Name}_${System.currentTimeMillis()}.xml")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        val expectedSavepoint1 =
            Savepoint(blankForm1.dbId, null, savepointFile1.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/$form1Name/$form1Name.xml")
        val expectedSavepoint2 =
            Savepoint(blankForm2.dbId, null, savepointFile2.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/$form2Name/$form2Name.xml")
        assertThat(savepoints, contains(expectedSavepoint1, expectedSavepoint2))
    }

    @Test
    fun ifThereAreMultipleVersionsOfTheSameBlankFormWithSavepoints_allSavepointsShouldBeImported() {
        val form1Name = "sampleForm"
        val form2Name = "sampleForm_1"

        // create blank forms
        val blankForm1 = createBlankForm(project, form1Name, "1", "1", date = 1)
        val blankForm2 = createBlankForm(project, form2Name, "1", "2", date = 2)

        // create savepoints
        val savepointFile1 = createSavepointFile(project, "${form1Name}_${System.currentTimeMillis()}.xml")
        val savepointFile2 = createSavepointFile(project, "${form2Name}_${System.currentTimeMillis()}.xml")

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        val expectedSavepoint1 =
            Savepoint(blankForm1.dbId, null, savepointFile1.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/$form1Name/$form1Name.xml")
        val expectedSavepoint2 =
            Savepoint(blankForm2.dbId, null, savepointFile2.absolutePath, "${storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid)}/$form2Name/$form2Name.xml")
        assertThat(savepoints, contains(expectedSavepoint2, expectedSavepoint1))
    }

    @Test
    fun ifASavedFormHasNoSavepoint_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1")

        // create saved forms
        createSavedForm(project, "sampleForm", form)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointCreatedEarlierThanTheForm_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1")

        // create saved forms
        val savedForm = createSavedForm(project, "sampleForm", form, lastStatusChangeDate = System.currentTimeMillis() + TimeInMs.ONE_HOUR)

        // create savepoints
        createSavepointFile(project, File(savedForm.instanceFilePath).name)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointButItsBlankFormIsSoftDeleted_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1", deleted = true)

        // create saved forms
        val savedForm = createSavedForm(project, "sampleForm", form)

        // create savepoints
        createSavepointFile(project, File(savedForm.instanceFilePath).name)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointButItsBlankFormDoesNotExist_nothingShouldBeImported() {
        // create saved forms
        val savedForm = createSavedForm(project, "sampleForm", FormFixtures.form("1"))

        // create savepoints
        createSavepointFile(project, File(savedForm.instanceFilePath).name)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        assertThat(savepoints.isEmpty(), equalTo(true))
    }

    @Test
    fun ifASavedFormHasASavepointButTheFormIsSoftDeleted_nothingShouldBeImported() {
        // create blank forms
        val form = createBlankForm(project, "sampleForm", "1")

        // create saved forms
        val savedForm = createSavedForm(project, "sampleForm", form, deletedDate = System.currentTimeMillis())

        // create savepoints
        createSavepointFile(project, File(savedForm.instanceFilePath).name)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
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
        val savedForm1 = createSavedForm(project, form1Name, form1)
        val savedForm2 = createSavedForm(project, form2Name, form2)

        // create savepoints
        val savepointFile1 = createSavepointFile(project, File(savedForm1.instanceFilePath).name)
        val savepointFile2 = createSavepointFile(project, File(savedForm2.instanceFilePath).name)

        // trigger importing
        savepointsImporter.run()

        // verify import
        val savepoints = savepointsRepositoryProvider.get(project.uuid).getAll()
        val expectedSavepoint1 = Savepoint(1, 1, savepointFile1.absolutePath, savedForm1.instanceFilePath)
        val expectedSavepoint2 = Savepoint(2, 2, savepointFile2.absolutePath, savedForm2.instanceFilePath)
        assertThat(savepoints, contains(expectedSavepoint1, expectedSavepoint2))
    }

    private fun createBlankForm(project: Project.Saved, formName: String, formId: String, formVersion: String = "1", date: Long = 0, deleted: Boolean = false): Form {
        val formFile = File(storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, project.uuid), "$formName.xml").also {
            it.writeText(RandomString.randomString(10))
        }
        val blankForm = formsRepositoryProvider.get(project.uuid).save(FormFixtures.form(formId, formVersion, formFile.absolutePath))
        return formsRepositoryProvider.get(project.uuid).save(Form.Builder(blankForm).date(date).deleted(deleted).build())
    }

    private fun createSavedForm(project: Project.Saved, formName: String, form: Form, lastStatusChangeDate: Long = System.currentTimeMillis() - TimeInMs.ONE_HOUR, deletedDate: Long? = null): Instance {
        return instancesRepositoryProvider.get(project.uuid).save(InstanceFixtures.instance(displayName = formName, form = form, lastStatusChangeDate = lastStatusChangeDate, deletedDate = deletedDate))
    }

    private fun createSavepointFile(project: Project.Saved, instanceName: String): File {
        val cacheDir = File(storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, project.uuid))
        return File(cacheDir, "$instanceName.save").also {
            it.writeText(RandomString.randomString(10))
        }
    }
}

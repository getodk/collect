package org.odk.collect.android.formmanagement

import android.app.Application
import android.database.ContentObserver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.provider.FormsProviderAPI.CONTENT_URI
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.BooleanChangeLock
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.FormSource
import org.odk.collect.formstest.FormUtils
import org.odk.collect.projects.Project
import org.odk.collect.shared.Md5.getMd5Hash

@RunWith(AndroidJUnit4::class)
class FormUpdateCheckerTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val component = DaggerUtils.getComponent(application)

    private val formsRepositoryProvider = component.formsRepositoryProvider()
    private val storagePathProvider = component.storagePathProvider()
    private val settingsProvider = component.settingsProvider()

    private val formSource = mock<FormSource> {
        on { fetchFormList() } doReturn emptyList()
    }

    private lateinit var updateChecker: FormUpdateChecker

    @Before
    fun setup() {
        val formSourceProvider = mock<FormSourceProvider> { on { get(any()) } doReturn formSource }

        updateChecker = FormUpdateChecker(
            context = application,
            notifier = mock(),
            analytics = mock(),
            changeLock = BooleanChangeLock(),
            storagePathProvider = storagePathProvider,
            settingsProvider = settingsProvider,
            formsRepositoryProvider = formsRepositoryProvider,
            formSourceProvider = formSourceProvider
        )
    }

    @Test
    fun `downloadUpdates() notifies Forms content resolver`() {
        val contentObserver = mock<ContentObserver>()
        application.contentResolver.registerContentObserver(CONTENT_URI, false, contentObserver)

        val project = setupProject()
        updateChecker.downloadUpdates(project.uuid)

        verify(contentObserver).dispatchChange(false, CONTENT_URI)
    }

    @Test
    fun `downloadUpdates() downloads updates when auto download is enabled`() {
        val project = setupProject()
        addFormLocally(project, "formId", "1")

        val updatedXForm = FormUtils.createXFormBody("formId", "2")
        addFormToServer(updatedXForm, "formId", "2")

        settingsProvider.getGeneralSettings(project.uuid)
            .save(GeneralKeys.KEY_AUTOMATIC_UPDATE, true)

        updateChecker.downloadUpdates(project.uuid)
        assertThat(
            formsRepositoryProvider.get(project.uuid).getAllByFormIdAndVersion("formId", "2").size,
            `is`(1)
        )
    }

    private fun addFormToServer(updatedXForm: String, formId: String, formVersion: String) {
        whenever(formSource.fetchFormList()).doReturn(
            listOf(
                FormListItem(
                    "http://$formId",
                    formId,
                    formVersion,
                    getMd5Hash(updatedXForm),
                    "blah",
                    null
                )
            )
        )
        whenever(formSource.fetchForm("http://$formId")).doAnswer { updatedXForm.byteInputStream() }
    }

    private fun addFormLocally(project: Project.Saved, formId: String, formVersion: String) {
        val formsDir = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, project.uuid)
        val formsRepository = formsRepositoryProvider.get(project.uuid)
        formsRepository.save(
            FormUtils.buildForm(formId, formVersion, formsDir).build()
        )
    }

    private fun setupProject(): Project.Saved {
        val projectImporter = component.projectImporter()
        return projectImporter.importNewProject(Project.New("blah", "B", "#ffffff"))
    }
}

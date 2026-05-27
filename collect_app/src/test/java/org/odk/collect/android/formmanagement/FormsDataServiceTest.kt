package org.odk.collect.android.formmanagement

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.ProjectDependencyModuleFactory
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.androidtest.recordValues
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.FormUtils.createXFormFile
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.locks.BooleanChangeLock
import org.odk.collect.shared.strings.Md5.getMd5Hash

@RunWith(AndroidJUnit4::class)
class FormsDataServiceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val component = DaggerUtils.getComponent(application)

    private val formsRepositoryProvider = component.formsRepositoryProvider()
    private val storagePathProvider = component.storagePathProvider()
    private val settingsProvider = component.settingsProvider()
    private val notifier = mock<Notifier>()
    private val analytics = mock<Analytics>()

    private val changeLockProvider = ChangeLockProvider { BooleanChangeLock() }

    private val formSource = mock<FormSource> {
        on { fetchFormList() } doReturn emptyList()
    }

    private lateinit var formsDataService: FormsDataService

    private lateinit var project: Project.Saved

    @Before
    fun setup() {
        project = setupProject()

        val projectDependencyModule = ProjectDependencyModule(
            project.uuid,
            { settingsProvider.getUnprotectedSettings(project.uuid) },
            formsRepositoryProvider,
            mock(),
            storagePathProvider,
            changeLockProvider,
            { formSource },
            mock(),
            mock(),
            mock()
        )

        val projectDependencyModuleFactory = mock<ProjectDependencyModuleFactory>()
        whenever(projectDependencyModuleFactory.create(project.uuid)).thenReturn(projectDependencyModule)

        formsDataService = FormsDataService(
            appState = AppState(),
            notifier = notifier,
            projectDependencyModuleFactory = projectDependencyModuleFactory
        ) { 0 }
    }

    @Test
    fun getServerError_isNullAtFirst() {
        assertThat(formsDataService.getServerError(project.uuid).value, equalTo(null))
    }

    @Test
    fun `downloadUpdates() downloads updates when auto download is enabled`() {
        addFormLocally(project, "formId", "1")

        val updatedXForm = FormUtils.createXFormBody("formId", "2")
        addFormToServer(updatedXForm, "formId", "2")

        settingsProvider.getUnprotectedSettings(project.uuid)
            .save(ProjectKeys.KEY_AUTOMATIC_UPDATE, true)

        formsDataService.downloadUpdates(project.uuid)
        assertThat(
            formsRepositoryProvider.create(project.uuid).getAllByFormIdAndVersion("formId", "2").size,
            equalTo(1)
        )
    }

    @Test
    fun `downloadUpdates() does nothing when change lock is locked`() {
        val isSyncing = formsDataService.isSyncing(project.uuid)

        val changeLock = changeLockProvider.create(project.uuid).formsLock as BooleanChangeLock
        changeLock.lock("blah")

        isSyncing.recordValues { projectValues ->
            formsDataService.downloadUpdates(project.uuid)
            verifyNoInteractions(formSource)
            verifyNoInteractions(notifier)
            verifyNoInteractions(analytics)

            assertThat(projectValues, equalTo(listOf(false)))
        }
    }

    @Test
    fun `matchFormsWithServer() does nothing when change lock is locked`() {
        val isSyncing = formsDataService.isSyncing(project.uuid)

        val changeLock = changeLockProvider.create(project.uuid).formsLock as BooleanChangeLock
        changeLock.lock("blah")

        isSyncing.recordValues { projectValues ->
            formsDataService.matchFormsWithServer(project.uuid)
            verifyNoInteractions(formSource)
            verifyNoInteractions(notifier)
            verifyNoInteractions(analytics)

            assertThat(projectValues, equalTo(listOf(false)))
        }
    }

    /**
     * We don't count calls where we can't acquire the lock as a "success". The front end should
     * protect against this actually coming up by not letting the user sync while a sync is running.
     */
    @Test
    fun `matchFormsWithServer() returns false when change lock is locked`() {
        val changeLock = changeLockProvider.create(project.uuid).formsLock as BooleanChangeLock
        changeLock.lock("blah")

        assertThat(formsDataService.matchFormsWithServer(project.uuid), equalTo(false))
    }

    @Test
    fun `matchFormsWithServer() returns false when there is an error communicating with the server`() {
        whenever(formSource.fetchFormList()).thenThrow(FormSourceException.FetchError())
        assertThat(formsDataService.matchFormsWithServer(project.uuid), equalTo(false))
    }

    @Test
    fun `matchFormsWithServer() updates project sync state`() {
        val projectState = formsDataService.isSyncing(project.uuid)
        val otherProjectState = formsDataService.isSyncing("other")

        projectState.recordValues { projectValues ->
            otherProjectState.recordValues { otherProjectValues ->
                formsDataService.matchFormsWithServer(project.uuid)

                assertThat(projectValues, equalTo(listOf(false, true, false)))
                assertThat(otherProjectValues, equalTo(listOf(false)))
            }
        }
    }

    @Test
    fun `matchFormsWithServer() when there is an error updates project error state`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid)

        assertThat(formsDataService.getServerError(project.uuid).getOrAwaitValue(), equalTo(error))
        assertThat(formsDataService.getServerError("other").getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `update() called after matchFormsWithServer() does not clear error state`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid)

        assertThat(formsDataService.getServerError(project.uuid).getOrAwaitValue(), equalTo(error))
        formsDataService.refresh(project.uuid)
        assertThat(formsDataService.getServerError(project.uuid).getOrAwaitValue(), equalTo(error))
    }

    @Test
    fun `matchFormsWithServer() notifies on error when called with default notify value`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid) { false }
        verify(notifier).onSync(error, project.uuid)
        verifyNoMoreInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() notifies on success when called with default notify value`() {
        formsDataService.matchFormsWithServer(project.uuid) { false }
        verify(notifier).onSync(null, project.uuid)
        verifyNoMoreInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() notifies on error when called with notify true`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid, true) { false }
        verify(notifier).onSync(error, project.uuid)
        verifyNoMoreInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() notifies on success when called with notify true`() {
        formsDataService.matchFormsWithServer(project.uuid, true) { false }
        verify(notifier).onSync(null, project.uuid)
        verifyNoMoreInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() does not notify on error when called with default isStopped value`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid, notify = false)
        verifyNoInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() does not notify on success when called with default isStopped value`() {
        formsDataService.matchFormsWithServer(project.uuid, notify = false)
        verifyNoInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() does not notify on error when called with isStopped false`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid, false) { false }
        verifyNoInteractions(notifier)
    }

    @Test
    fun `matchFormsWithServer() notifies on error when called with isStopped true`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid, false) { true }
        verify(notifier).onSyncStopped(project.uuid)
        verifyNoMoreInteractions(notifier)
    }

    @Test
    fun `clear() clears error state`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid)

        formsDataService.clear(project.uuid)
        assertThat(formsDataService.getServerError(project.uuid).getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `update() does nothing when change lock is locked`() {
        val isSyncing = formsDataService.isSyncing(project.uuid)

        val changeLock = changeLockProvider.create(project.uuid).formsLock as BooleanChangeLock
        changeLock.lock("blah")

        isSyncing.recordValues { projectValues ->
            formsDataService.refresh(project.uuid)
            assertThat(projectValues, equalTo(listOf(false)))
        }
    }

    @Test
    fun `#getFormsCount ignores soft-deleted forms`() {
        formsRepositoryProvider.create(project.uuid).apply {
            save(
                Form.Builder()
                    .displayName("1")
                    .formId("1")
                    .version("1")
                    .deleted(false)
                    .formFilePath(createXFormFile("1", "1").absolutePath)
                    .build()
            )
            save(
                Form.Builder()
                    .displayName("2")
                    .formId("2")
                    .version("1")
                    .deleted(true)
                    .formFilePath(createXFormFile("2", "1").absolutePath)
                    .build()
            )
        }
        formsDataService.update(project.uuid)
        assertThat(formsDataService.getFormsCount(project.uuid).value, equalTo(1))
    }

    private fun addFormToServer(updatedXForm: String, formId: String, formVersion: String) {
        whenever(formSource.fetchFormList()).doReturn(
            listOf(
                FormListItem(
                    "http://$formId",
                    formId,
                    formVersion,
                    updatedXForm.getMd5Hash(),
                    "blah",
                    null
                )
            )
        )
        whenever(formSource.fetchForm("http://$formId")).doAnswer { updatedXForm.byteInputStream() }
    }

    private fun addFormLocally(project: Project.Saved, formId: String, formVersion: String) {
        val formsDir = storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, project.uuid)
        val formsRepository = formsRepositoryProvider.create(project.uuid)
        formsRepository.save(
            FormUtils.buildForm(formId, formVersion, formsDir).build()
        )
    }

    private fun setupProject(): Project.Saved {
        val projectsRepository = component.projectsRepository()
        return projectsRepository.save(Project.New("blah", "B", "#ffffff"))
    }
}

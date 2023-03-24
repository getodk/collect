package org.odk.collect.android.formmanagement

import android.app.Application
import android.database.ContentObserver
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyProvider
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.androidtest.recordValues
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.formstest.FormUtils
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.strings.Md5.getMd5Hash
import org.odk.collect.testshared.BooleanChangeLock

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

    private val changeLockProvider = mock<ChangeLockProvider> {
        on { getFormLock(any()) } doReturn BooleanChangeLock()
    }

    private val formSource = mock<FormSource> {
        on { fetchFormList() } doReturn emptyList()
    }

    private lateinit var formsDataService: FormsDataService

    private lateinit var project: Project.Saved

    @Before
    fun setup() {
        project = setupProject()

        val formSourceProvider = mock<FormSourceProvider> { on { get(any()) } doReturn formSource }

        val projectDependencyProvider = ProjectDependencyProvider(
            project.uuid,
            settingsProvider,
            formsRepositoryProvider,
            mock(),
            storagePathProvider,
            changeLockProvider,
            formSourceProvider
        )

        val projectDependencyProviderFactory = mock<ProjectDependencyProviderFactory>()
        whenever(projectDependencyProviderFactory.create(project.uuid)).thenReturn(projectDependencyProvider)

        formsDataService = FormsDataService(
            context = application,
            notifier = notifier,
            projectDependencyProviderFactory = projectDependencyProviderFactory
        ) { 0 }
    }

    @Test
    fun getSyncError_isNullAtFirst() {
        assertThat(formsDataService.getSyncError(project.uuid).value, equalTo(null))
    }

    @Test
    fun `downloadUpdates() notifies Forms content resolver`() {
        val contentObserver = mock<ContentObserver>()
        application.contentResolver.registerContentObserver(
            FormsContract.getUri(project.uuid),
            false,
            contentObserver
        )

        formsDataService.downloadUpdates(project.uuid)

        verify(contentObserver).dispatchChange(false, FormsContract.getUri(project.uuid))
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
            formsRepositoryProvider.get(project.uuid).getAllByFormIdAndVersion("formId", "2").size,
            equalTo(1)
        )
    }

    @Test
    fun `matchFormsWithServer() does nothing when change lock is locked`() {
        val changeLock = BooleanChangeLock()
        whenever(changeLockProvider.getFormLock(project.uuid)).thenReturn(changeLock)
        changeLock.lock()

        formsDataService.matchFormsWithServer(project.uuid)
        assertThat(formsDataService.isSyncing(project.uuid).getOrAwaitValue(), equalTo(false))
        verifyNoInteractions(formSource)
        verifyNoInteractions(notifier)
        verifyNoInteractions(analytics)
    }

    /**
     * We don't count calls where we can't acquire the lock as a "success". The front end should
     * protect against this actually coming up by not letting the user sync while a sync is running.
     */
    @Test
    fun `matchFormsWithServer() returns false when change lock is locked`() {
        val changeLock = BooleanChangeLock()
        whenever(changeLockProvider.getFormLock(project.uuid)).thenReturn(changeLock)
        changeLock.lock()

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

        assertThat(formsDataService.getSyncError(project.uuid).getOrAwaitValue(), equalTo(error))
        assertThat(formsDataService.getSyncError("other").getOrAwaitValue(), equalTo(null))
    }

    @Test
    fun `matchFormsWithServer() notifies when called with default notify value`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid)
        verify(notifier).onSync(error, project.uuid)
    }

    @Test
    fun `matchFormsWithServer() notifies when called with notify true`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid, true)
        verify(notifier).onSync(error, project.uuid)
    }

    @Test
    fun `matchFormsWithServer() does not notify when called with notify false`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid, false)
        verifyNoInteractions(notifier)
    }

    @Test
    fun `clear() clears error state`() {
        val error = FormSourceException.FetchError()
        whenever(formSource.fetchFormList()).thenThrow(error)
        formsDataService.matchFormsWithServer(project.uuid)

        formsDataService.clear(project.uuid)
        assertThat(formsDataService.getSyncError(project.uuid).getOrAwaitValue(), equalTo(null))
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
        val projectsRepository = component.projectsRepository()
        return projectsRepository.save(Project.New("blah", "B", "#ffffff"))
    }
}

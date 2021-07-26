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
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.FormSource
import org.odk.collect.forms.FormSourceException
import org.odk.collect.formstest.FormUtils
import org.odk.collect.projects.Project
import org.odk.collect.shared.strings.Md5.getMd5Hash
import org.odk.collect.testshared.BooleanChangeLock

@RunWith(AndroidJUnit4::class)
class FormsUpdaterTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val component = DaggerUtils.getComponent(application)

    private val formsRepositoryProvider = component.formsRepositoryProvider()
    private val storagePathProvider = component.storagePathProvider()
    private val settingsProvider = component.settingsProvider()
    private val syncStatusAppState = mock<SyncStatusAppState>()
    private val notifier = mock<Notifier>()
    private val analytics = mock<Analytics>()

    private val changeLockProvider = mock<ChangeLockProvider> {
        on { getFormLock(any()) } doReturn BooleanChangeLock()
    }

    private val formSource = mock<FormSource> {
        on { fetchFormList() } doReturn emptyList()
    }

    private lateinit var updateManager: FormsUpdater

    @Before
    fun setup() {
        val formSourceProvider = mock<FormSourceProvider> { on { get(any()) } doReturn formSource }

        updateManager = FormsUpdater(
            context = application,
            notifier = mock(),
            analytics = mock(),
            storagePathProvider = storagePathProvider,
            settingsProvider = settingsProvider,
            formsRepositoryProvider = formsRepositoryProvider,
            formSourceProvider = formSourceProvider,
            syncStatusAppState = syncStatusAppState,
            instancesRepositoryProvider = mock(),
            changeLockProvider
        )
    }

    @Test
    fun `downloadUpdates() notifies Forms content resolver`() {
        val project = setupProject()

        val contentObserver = mock<ContentObserver>()
        application.contentResolver.registerContentObserver(
            FormsContract.getUri(project.uuid),
            false,
            contentObserver
        )

        updateManager.downloadUpdates(project.uuid)

        verify(contentObserver).dispatchChange(false, FormsContract.getUri(project.uuid))
    }

    @Test
    fun `downloadUpdates() downloads updates when auto download is enabled`() {
        val project = setupProject()
        addFormLocally(project, "formId", "1")

        val updatedXForm = FormUtils.createXFormBody("formId", "2")
        addFormToServer(updatedXForm, "formId", "2")

        settingsProvider.getGeneralSettings(project.uuid)
            .save(ProjectKeys.KEY_AUTOMATIC_UPDATE, true)

        updateManager.downloadUpdates(project.uuid)
        assertThat(
            formsRepositoryProvider.get(project.uuid).getAllByFormIdAndVersion("formId", "2").size,
            `is`(1)
        )
    }

    @Test
    fun `matchFormsWithServer() does nothing when change lock is locked`() {
        val project = setupProject()

        val changeLock = BooleanChangeLock()
        whenever(changeLockProvider.getFormLock(project.uuid)).thenReturn(changeLock)
        changeLock.lock()

        updateManager.matchFormsWithServer(project.uuid)
        verifyNoInteractions(syncStatusAppState)
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
        val project = setupProject()

        val changeLock = BooleanChangeLock()
        whenever(changeLockProvider.getFormLock(project.uuid)).thenReturn(changeLock)
        changeLock.lock()

        assertThat(updateManager.matchFormsWithServer(project.uuid), `is`(false))
    }

    @Test
    fun `matchFormsWithServer() returns false when there is an error communicating with the server`() {
        val project = setupProject()

        whenever(formSource.fetchFormList()).thenThrow(FormSourceException.FetchError())
        assertThat(updateManager.matchFormsWithServer(project.uuid), `is`(false))
    }

    @Test
    fun `matchFormsWithServer() updates sync state`() {
        val project = setupProject()

        val inOrder = inOrder(syncStatusAppState)
        updateManager.matchFormsWithServer(project.uuid)
        inOrder.verify(syncStatusAppState).startSync(project.uuid)
        inOrder.verify(syncStatusAppState).finishSync(project.uuid, null)
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

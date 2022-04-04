package org.odk.collect.android.formlist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertFalse
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidtest.LiveDataTester
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSourceException
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.testshared.BooleanChangeLock
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class FormListViewModelTest {
    private val form1 = Form.Builder()
        .dbId(1)
        .formId("2")
        .version("1")
        .displayName("Form 2")
        .date(0)
        .formFilePath(FormUtils.createXFormFile("2", "1").absolutePath)
        .build()

    private val form2 = Form.Builder()
        .dbId(2)
        .formId("1")
        .version("1")
        .displayName("Form 1")
        .date(1)
        .formFilePath(FormUtils.createXFormFile("1", "1").absolutePath)
        .build()

    private val form3 = Form.Builder()
        .dbId(3)
        .formId("2x")
        .version("1")
        .displayName("Form 2x")
        .date(3)
        .formFilePath(FormUtils.createXFormFile("2x", "1").absolutePath)
        .build()

    private val form4 = Form.Builder()
        .dbId(4)
        .formId("4")
        .version("1")
        .displayName("Form 4")
        .date(3)
        .deleted(true)
        .formFilePath(FormUtils.createXFormFile("4", "1").absolutePath)
        .build()

    private val formsRepository = InMemFormsRepository().apply {
        save(form1)
        save(form2)
        save(form3)
        save(form4)
    }
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val syncRepository: SyncStatusAppState = mock()
    private val formsUpdater: FormsUpdater = mock()
    private val scheduler = FakeScheduler()
    private val generalSettings = InMemSettings()
    private val analytics: Analytics = mock()
    private val changeLockProvider: ChangeLockProvider = mock()
    private val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer = mock()
    private val projectId = "projectId"

    private val changeLock = BooleanChangeLock()
    private lateinit var viewModel: FormListViewModel

    private val formListItem1 = FormListItem(
        databaseId = form1.dbId,
        formId = form1.dbId,
        formName = form1.displayName,
        formVersion = form1.version ?: "",
        geometryPath = form1.geometryXpath ?: "",
        dateOfCreation = form1.date,
        dateOfLastUsage = 0,
        contentUri = FormsContract.getUri(projectId, form1.dbId)
    )

    private val formListItem2 = FormListItem(
        databaseId = form2.dbId,
        formId = form2.dbId,
        formName = form2.displayName,
        formVersion = form2.version ?: "",
        geometryPath = form2.geometryXpath ?: "",
        dateOfCreation = form2.date,
        dateOfLastUsage = 0,
        contentUri = FormsContract.getUri(projectId, form2.dbId)
    )

    private val formListItem3 = FormListItem(
        databaseId = form3.dbId,
        formId = form3.dbId,
        formName = form3.displayName,
        formVersion = form3.version ?: "",
        geometryPath = form3.geometryXpath ?: "",
        dateOfCreation = form3.date,
        dateOfLastUsage = 0,
        contentUri = FormsContract.getUri(projectId, form3.dbId)
    )

    private val formListItem4 = FormListItem(
        databaseId = form4.dbId,
        formId = form4.dbId,
        formName = form4.displayName,
        formVersion = form4.version ?: "",
        geometryPath = form4.geometryXpath ?: "",
        dateOfCreation = form4.date,
        dateOfLastUsage = 0,
        contentUri = FormsContract.getUri(projectId, form4.dbId)
    )

    @Before
    fun setup() {
        whenever(changeLockProvider.getFormLock(projectId)).thenReturn(changeLock)
        whenever(formsDirDiskFormsSynchronizer.synchronizeAndReturnError()).thenReturn("Result text")

        viewModel = FormListViewModel(
            formsRepository,
            context,
            syncRepository,
            formsUpdater,
            scheduler,
            generalSettings,
            analytics,
            changeLockProvider,
            formsDirDiskFormsSynchronizer,
            projectId
        )

        generalSettings.save(ProjectKeys.KEY_SERVER_URL, "https://sample.com")
    }

    @Test
    fun `syncWithStorage should be triggered when viewModel is initialized`() {
        verify(formsDirDiskFormsSynchronizer).synchronizeAndReturnError()
    }

    @Test
    fun `syncWithStorage return correct result`() {
        assertThat(viewModel.syncResult.value?.value, `is`("Result text"))
    }

    @Test
    fun `syncWithStorage should not be triggered when viewModel is initialized if another process operates on forms database`() {
        changeLock.lock()
        val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer = mock()

        FormListViewModel(
            formsRepository,
            context,
            syncRepository,
            formsUpdater,
            scheduler,
            generalSettings,
            analytics,
            changeLockProvider,
            formsDirDiskFormsSynchronizer,
            projectId
        )

        verifyNoInteractions(formsDirDiskFormsSynchronizer)
    }

    @Test
    fun `syncWithServer when task finishes sets result to true`() {
        doReturn(true).whenever(formsUpdater).matchFormsWithServer(projectId)
        val result = viewModel.syncWithServer()
        scheduler.runBackground()
        assertThat(result.value, `is`(true))
    }

    @Test
    fun `syncWithServer when there is an error sets result to false`() {
        doReturn(false).whenever(formsUpdater).matchFormsWithServer(projectId)
        val result = viewModel.syncWithServer()
        scheduler.runBackground()
        assertThat(result.value, `is`(false))
    }

    @Test
    fun `isMatchExactlyEnabled returns correct value based on settings`() {
        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)

        assertThat(viewModel.isMatchExactlyEnabled(), `is`(false))

        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        assertThat(viewModel.isMatchExactlyEnabled(), `is`(true))
    }

    @Test
    fun `isSyncingWithServer follows repository isSyncing`() {
        val liveData = MutableLiveData(true)
        whenever(syncRepository.isSyncing(projectId)).thenReturn(liveData)

        assertThat(viewModel.isSyncingWithServer().value, `is`(true))
        liveData.value = false
        assertThat(viewModel.isSyncingWithServer().value, `is`(false))
    }

    @Test
    fun `isOutOfSyncWithServer follows repository syncError`() {
        val liveData = MutableLiveData<FormSourceException?>(FormSourceException.FetchError())
        whenever(syncRepository.getSyncError(projectId)).thenReturn(liveData)

        val outOfSync = LiveDataTester().activate(viewModel.isOutOfSyncWithServer())
        assertThat(outOfSync.value, `is`(true))
        liveData.value = null
        assertThat(outOfSync.value, `is`(false))
    }

    @Test
    fun `isAuthenticationRequired follows repository syncError`() {
        val liveData = MutableLiveData<FormSourceException?>(FormSourceException.FetchError())
        whenever(syncRepository.getSyncError(projectId)).thenReturn(liveData)

        val authenticationRequired = LiveDataTester().activate(viewModel.isAuthenticationRequired())
        assertThat(authenticationRequired.value, `is`(false))
        liveData.value = FormSourceException.AuthRequired()
        assertThat(authenticationRequired.value, `is`(true))
        liveData.value = null
        assertThat(authenticationRequired.value, `is`(false))
    }

    @Test
    fun `original list of forms should be fetched from database initially and sorted by name ASC`() {
        val firstForm = viewModel.formsToDisplay.value!![0]
        val secondForm = viewModel.formsToDisplay.value!![1]
        val thirdForm = viewModel.formsToDisplay.value!![2]

        assertThat(firstForm, `is`(formListItem2))
        assertThat(secondForm, `is`(formListItem1))
        assertThat(thirdForm, `is`(formListItem3))
    }

    @Test
    fun `deleted forms should be ignored`() {
        assertThat(viewModel.formsToDisplay.value!!.size, `is`(3))
        assertFalse(viewModel.formsToDisplay.value!!.contains(formListItem4))
    }

    @Test
    fun `list of forms should be sorted when sorting order is changed`() {
        // Sort by name DESC
        viewModel.sortingOrder = 1

        var firstForm = viewModel.formsToDisplay.value!![0]
        var secondForm = viewModel.formsToDisplay.value!![1]
        var thirdForm = viewModel.formsToDisplay.value!![2]

        assertThat(firstForm, `is`(formListItem3))
        assertThat(secondForm, `is`(formListItem1))
        assertThat(thirdForm, `is`(formListItem2))

        // Sort by date ASC
        viewModel.sortingOrder = 2

        firstForm = viewModel.formsToDisplay.value!![0]
        secondForm = viewModel.formsToDisplay.value!![1]
        thirdForm = viewModel.formsToDisplay.value!![2]

        assertThat(firstForm, `is`(formListItem1))
        assertThat(secondForm, `is`(formListItem2))
        assertThat(thirdForm, `is`(formListItem3))

        // Sort by date DESC
        viewModel.sortingOrder = 3

        firstForm = viewModel.formsToDisplay.value!![0]
        secondForm = viewModel.formsToDisplay.value!![1]
        thirdForm = viewModel.formsToDisplay.value!![2]

        assertThat(firstForm, `is`(formListItem3))
        assertThat(secondForm, `is`(formListItem2))
        assertThat(thirdForm, `is`(formListItem1))

        // Sort by name ASC
        viewModel.sortingOrder = 0

        firstForm = viewModel.formsToDisplay.value!![0]
        secondForm = viewModel.formsToDisplay.value!![1]
        thirdForm = viewModel.formsToDisplay.value!![2]

        assertThat(firstForm, `is`(formListItem2))
        assertThat(secondForm, `is`(formListItem1))
        assertThat(thirdForm, `is`(formListItem3))
    }

    @Test
    fun `list of forms should be filtered when filterText is changed`() {
        viewModel.filterText = "2"

        var firstForm = viewModel.formsToDisplay.value!![0]
        var secondForm = viewModel.formsToDisplay.value!![1]

        assertThat(viewModel.formsToDisplay.value?.size, `is`(2))
        assertThat(firstForm, `is`(formListItem1))
        assertThat(secondForm, `is`(formListItem3))

        viewModel.filterText = "2x"

        firstForm = viewModel.formsToDisplay.value!![0]

        assertThat(viewModel.formsToDisplay.value?.size, `is`(1))
        assertThat(firstForm, `is`(formListItem3))

        viewModel.filterText = ""

        firstForm = viewModel.formsToDisplay.value!![0]
        secondForm = viewModel.formsToDisplay.value!![1]
        val thirdForm = viewModel.formsToDisplay.value!![2]

        assertThat(viewModel.formsToDisplay.value?.size, `is`(3))
        assertThat(firstForm, `is`(formListItem2))
        assertThat(secondForm, `is`(formListItem1))
        assertThat(thirdForm, `is`(formListItem3))
    }

    @Test
    fun `filtering and sorting should work together`() {
        viewModel.filterText = "2"

        var firstForm = viewModel.formsToDisplay.value!![0]
        var secondForm = viewModel.formsToDisplay.value!![1]

        assertThat(viewModel.formsToDisplay.value?.size, `is`(2))
        assertThat(firstForm, `is`(formListItem1))
        assertThat(secondForm, `is`(formListItem3))

        viewModel.sortingOrder = 1

        firstForm = viewModel.formsToDisplay.value!![0]
        secondForm = viewModel.formsToDisplay.value!![1]

        assertThat(viewModel.formsToDisplay.value?.size, `is`(2))
        assertThat(firstForm, `is`(formListItem3))
        assertThat(secondForm, `is`(formListItem1))
    }
}

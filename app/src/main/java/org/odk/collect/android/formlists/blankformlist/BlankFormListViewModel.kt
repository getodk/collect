package org.odk.collect.android.formlists.blankformlist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormSourceException.AuthRequired
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream

class BlankFormListViewModel(
    private val formsRepository: FormsRepository,
    private val instancesRepository: InstancesRepository,
    private val application: Application,
    private val syncRepository: SyncStatusAppState,
    private val formsUpdater: FormsUpdater,
    private val scheduler: Scheduler,
    private val generalSettings: Settings,
    private val changeLockProvider: ChangeLockProvider,
    private val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer,
    private val projectId: String
) : ViewModel() {

    private val _allForms: MutableNonNullLiveData<List<BlankFormListItem>> = MutableNonNullLiveData(emptyList())
    private val _formsToDisplay: MutableLiveData<List<BlankFormListItem>?> = MutableLiveData()
    val formsToDisplay: LiveData<List<BlankFormListItem>?> = _formsToDisplay

    private val _syncResult: MutableLiveData<Consumable<String>> = MutableLiveData()
    val syncResult: LiveData<Consumable<String>> = _syncResult

    private val isFormLoadingRunning = MutableNonNullLiveData(false)
    private val isSyncingWithStorageRunning = MutableNonNullLiveData(false)

    val isLoading: LiveData<Boolean> = Transformations.map(
        LiveDataUtils.zip3(
            isFormLoadingRunning,
            isSyncingWithStorageRunning,
            syncRepository.isSyncing(projectId),
        )
    ) { (one, two, three) -> one || two || three }

    var sortingOrder: Int = generalSettings.getInt("formChooserListSortingOrder")
        get() { return generalSettings.getInt("formChooserListSortingOrder") }

        set(value) {
            field = value
            generalSettings.save("formChooserListSortingOrder", value)
            sortAndFilter()
        }

    var filterText: String = ""
        set(value) {
            field = value
            sortAndFilter()
        }

    private val shouldHideOldFormVersions: Boolean
        get() {
            return generalSettings.getBoolean(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)
        }

    private val syncWithServerObserver = Observer<Boolean> {
        if (!it) {
            loadFromDatabase()
        }
    }

    init {
        loadFromDatabase()
        syncWithStorage()

        syncRepository.isSyncing(projectId).observeForever(syncWithServerObserver)
    }

    fun getAllForms(): List<BlankFormListItem> {
        return formsRepository
            .all
            .map { form ->
                form.toBlankFormListItem(projectId, instancesRepository)
            }
    }

    private fun loadFromDatabase() {
        isFormLoadingRunning.value = true
        scheduler.immediate(
            background = {
                var newListOfForms = formsRepository
                    .all
                    .filter {
                        !it.isDeleted
                    }.map { form ->
                        form.toBlankFormListItem(projectId, instancesRepository)
                    }

                if (shouldHideOldFormVersions) {
                    newListOfForms = newListOfForms.groupBy {
                        it.formId
                    }.map { (_, itemsWithSameId) ->
                        itemsWithSameId.sortedBy {
                            it.dateOfCreation
                        }.last()
                    }
                }
                newListOfForms
            },
            foreground = { newListOfForms ->
                _allForms.value = newListOfForms.toList()
                sortAndFilter()
                isFormLoadingRunning.value = false
            }
        )
    }

    private fun syncWithStorage() {
        changeLockProvider.getFormLock(projectId).withLock { acquiredLock ->
            if (acquiredLock) {
                isSyncingWithStorageRunning.value = true
                scheduler.immediate(
                    background = {
                        formsDirDiskFormsSynchronizer.synchronizeAndReturnError()
                    },
                    foreground = { result: String? ->
                        result?.let {
                            loadFromDatabase()
                            _syncResult.value = Consumable(result)
                        }
                        isSyncingWithStorageRunning.value = false
                    }
                )
            }
        }
    }

    fun syncWithServer(): LiveData<Boolean> {
        logManualSyncWithServer()
        val result = MutableLiveData<Boolean>()
        scheduler.immediate(
            { formsUpdater.matchFormsWithServer(projectId) },
            { value: Boolean ->
                loadFromDatabase()
                result.value = value
            }
        )
        return result
    }

    fun isMatchExactlyEnabled(): Boolean {
        return SettingsUtils.getFormUpdateMode(
            application,
            generalSettings
        ) == FormUpdateMode.MATCH_EXACTLY
    }

    fun isOutOfSyncWithServer(): LiveData<Boolean> {
        return Transformations.map(
            syncRepository.getSyncError(projectId)
        ) { obj: FormSourceException? ->
            obj != null
        }
    }

    fun isAuthenticationRequired(): LiveData<Boolean> {
        return Transformations.map(
            syncRepository.getSyncError(projectId)
        ) { error: FormSourceException? ->
            if (error != null) {
                error is AuthRequired
            } else {
                false
            }
        }
    }

    private fun logManualSyncWithServer() {
        val uri = Uri.parse(generalSettings.getString(ProjectKeys.KEY_SERVER_URL))
        val host = if (uri.host != null) uri.host else ""
        val urlHash = getMd5Hash(ByteArrayInputStream(host!!.toByteArray())) ?: ""
    }

    private fun sortAndFilter() {
        _formsToDisplay.value = when (sortingOrder) {
            0 -> _allForms.value.sortedBy { it.formName.lowercase() }
            1 -> _allForms.value.sortedByDescending { it.formName.lowercase() }
            2 -> _allForms.value.sortedByDescending { it.dateOfLastDetectedAttachmentsUpdate ?: it.dateOfCreation }
            3 -> _allForms.value.sortedBy { it.dateOfLastDetectedAttachmentsUpdate ?: it.dateOfCreation }
            4 -> _allForms.value.sortedByDescending { it.dateOfLastUsage }
            else -> { _allForms.value }
        }.filter {
            filterText.isBlank() || it.formName.contains(filterText, true)
        }
    }

    class Factory(
        private val formsRepository: FormsRepository,
        private val instancesRepository: InstancesRepository,
        private val application: Application,
        private val syncRepository: SyncStatusAppState,
        private val formsUpdater: FormsUpdater,
        private val scheduler: Scheduler,
        private val generalSettings: Settings,
        private val changeLockProvider: ChangeLockProvider,
        private val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer,
        private val projectId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BlankFormListViewModel(
                formsRepository,
                instancesRepository,
                application,
                syncRepository,
                formsUpdater,
                scheduler,
                generalSettings,
                changeLockProvider,
                formsDirDiskFormsSynchronizer,
                projectId
            ) as T
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncRepository.isSyncing(projectId).removeObserver(syncWithServerObserver)
    }
}

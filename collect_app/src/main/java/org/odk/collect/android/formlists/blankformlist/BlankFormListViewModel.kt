package org.odk.collect.android.formlists.blankformlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.formmanagement.matchexactly.SyncDataService
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormSourceException.AuthRequired
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

class BlankFormListViewModel(
    private val instancesRepository: InstancesRepository,
    private val application: Application,
    private val formsDataService: FormsDataService,
    private val scheduler: Scheduler,
    private val generalSettings: Settings,
    private val projectId: String,
    private val showAllVersions: Boolean = false
) : ViewModel() {

    private val _allForms: MutableNonNullLiveData<List<BlankFormListItem>> = MutableNonNullLiveData(emptyList())
    private val _formsToDisplay: MutableLiveData<List<BlankFormListItem>?> = MutableLiveData()
    val formsToDisplay: LiveData<List<BlankFormListItem>?> = formsDataService.getForms(projectId).map { forms ->
        var newListOfForms = forms
            .filter {
                !it.isDeleted
            }.map { form ->
                form.toBlankFormListItem(projectId, instancesRepository)
            }

        if (shouldHideOldFormVersions && !showAllVersions) {
            newListOfForms = newListOfForms.groupBy {
                it.formId
            }.map { (_, itemsWithSameId) ->
                itemsWithSameId.sortedBy {
                    it.dateOfCreation
                }.last()
            }
        }
        newListOfForms
    }

    val syncResult: LiveData<String?> = formsDataService.getDiskError(projectId)

    private val isFormLoadingRunning = MutableNonNullLiveData(false)
    private val isSyncingWithStorageRunning = MutableNonNullLiveData(false)

    val isLoading: LiveData<Boolean> = LiveDataUtils.zip3(
        isFormLoadingRunning,
        isSyncingWithStorageRunning,
        formsDataService.isSyncing(projectId)
    ).map { (one, two, three) -> one || two || three }

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

    init {
        scheduler.immediate(background = {
            formsDataService.update(projectId)
        }, foreground = {

        })
    }

    fun getAllForms(): List<BlankFormListItem> {
        return formsDataService
            .all(projectId)
            .map { form ->
                form.toBlankFormListItem(projectId, instancesRepository)
            }
    }

    fun syncWithServer(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        scheduler.immediate(
            { formsDataService.matchFormsWithServer(projectId) },
            { value: Boolean ->
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
        return formsDataService.getSyncError(projectId).map { obj: FormSourceException? ->
            obj != null
        }
    }

    fun isAuthenticationRequired(): LiveData<Boolean> {
        return formsDataService.getSyncError(projectId).map { error: FormSourceException? ->
            if (error != null) {
                error is AuthRequired
            } else {
                false
            }
        }
    }

    fun deleteForms(vararg databaseIds: Long) {
        scheduler.immediate(
            background = {
                databaseIds.forEach {
                    formsDataService.deleteForm(projectId, it)
                }
            },
            foreground = {}
        )
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
        private val syncRepository: SyncDataService,
        private val formsDataService: FormsDataService,
        private val scheduler: Scheduler,
        private val generalSettings: Settings,
        private val changeLockProvider: ChangeLockProvider,
        private val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer,
        private val projectId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BlankFormListViewModel(
                instancesRepository,
                application,
                formsDataService,
                scheduler,
                generalSettings,
                projectId,
                !generalSettings.getBoolean(ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)
            ) as T
        }
    }
}

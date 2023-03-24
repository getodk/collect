package org.odk.collect.android.formlists.blankformlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormSourceException.AuthRequired
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

    private val _filterText = MutableLiveData("")
    private val _sortingOrder = MutableLiveData(generalSettings.getInt("formChooserListSortingOrder"))
    private val filteredForms = LiveDataUtils.zip3(formsDataService.getForms(projectId), _filterText, _sortingOrder)
    val formsToDisplay: LiveData<List<BlankFormListItem>?> = filteredForms.map { (forms, filter, sort) ->
        filterAndSortForms(forms, sort, filter)
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
            _sortingOrder.value = value
        }

    var filterText: String = ""
        set(value) {
            field = value
            _filterText.value = value
        }

    init {
        scheduler.immediate(
            background = {
                formsDataService.update(projectId)
            },
            foreground = {}
        )
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

    private fun filterAndSortForms(
        forms: List<Form>?,
        sort: Int?,
        filter: String
    ): List<BlankFormListItem> {
        if (forms == null) {
            return emptyList()
        }

        var newListOfForms = forms
            .filter {
                !it.isDeleted
            }.map { form ->
                form.toBlankFormListItem(projectId, instancesRepository)
            }

        if (!showAllVersions) {
            newListOfForms = newListOfForms.groupBy {
                it.formId
            }.map { (_, itemsWithSameId) ->
                itemsWithSameId.sortedBy {
                    it.dateOfCreation
                }.last()
            }
        }

        return when (sort) {
            0 -> newListOfForms.sortedBy { it.formName.lowercase() }
            1 -> newListOfForms.sortedByDescending { it.formName.lowercase() }
            2 -> newListOfForms.sortedByDescending {
                it.dateOfLastDetectedAttachmentsUpdate ?: it.dateOfCreation
            }
            3 -> newListOfForms.sortedBy {
                it.dateOfLastDetectedAttachmentsUpdate ?: it.dateOfCreation
            }
            4 -> newListOfForms.sortedByDescending { it.dateOfLastUsage }
            else -> {
                newListOfForms
            }
        }.filter {
            filter.isBlank() || it.formName.contains(filter, true)
        }
    }

    class Factory(
        private val instancesRepository: InstancesRepository,
        private val application: Application,
        private val formsDataService: FormsDataService,
        private val scheduler: Scheduler,
        private val generalSettings: Settings,
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

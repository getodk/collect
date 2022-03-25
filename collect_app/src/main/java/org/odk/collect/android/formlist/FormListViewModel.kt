package org.odk.collect.android.formlist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.preferences.utilities.SettingsUtils
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.FormSourceException.AuthRequired
import org.odk.collect.forms.FormsRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream

class FormListViewModel(
    private val formsRepository: FormsRepository,
    private val application: Application,
    private val syncRepository: SyncStatusAppState,
    private val formsUpdater: FormsUpdater,
    private val scheduler: Scheduler,
    private val generalSettings: Settings,
    private val analytics: Analytics,
    private val projectId: String
) : ViewModel() {

    private val _syncResult: MutableLiveData<Consumable<String>> = MutableLiveData()
    val syncResult: LiveData<Consumable<String>> = _syncResult

    private val _forms: MutableLiveData<Consumable<List<FormListItem>>> = MutableLiveData()
    val forms: LiveData<Consumable<List<FormListItem>>> = _forms

    val filterText = MutableNonNullLiveData("")

    private val sortingOrderObserver = Observer<Int> { newSortingOrder ->
        generalSettings.save("formChooserListSortingOrder", newSortingOrder)
    }
    val sortingOrder = MutableNonNullLiveData(generalSettings.getInt("formChooserListSortingOrder")).apply {
        observeForever(sortingOrderObserver)
    }

    private val _showProgressBar: MutableLiveData<Consumable<Boolean>> = MutableLiveData()
    val showProgressBar: LiveData<Consumable<Boolean>> = _showProgressBar

    val sortingOptions = intArrayOf(
        R.string.sort_by_name_asc,
        R.string.sort_by_name_desc,
        R.string.sort_by_date_asc,
        R.string.sort_by_date_desc
    )

    init {
        fetchForms()
        syncForms()
    }

    private fun fetchForms() {
        viewModelScope.launch {
            _showProgressBar.value = Consumable(true)

            _forms.value = Consumable(
                formsRepository
                    .all
                    .map { form ->
                        FormListItem(
                            formId = form.dbId,
                            formName = form.displayName,
                            formVersion = form.version ?: "",
                            geometryPath = form.geometryXpath ?: "",
                            dateOfCreation = form.date,
                            dateOfLastUsage = 0
                        )
                    }
                    .toList()
            )

            _showProgressBar.value = Consumable(false)
        }
    }

    private fun syncForms() {
        viewModelScope.launch {
            _showProgressBar.value = Consumable(true)

            val result = FormsDirDiskFormsSynchronizer().synchronizeAndReturnError()
            fetchForms()
            _syncResult.value = Consumable(result)

            _showProgressBar.value = Consumable(false)
        }
    }

    fun isMatchExactlyEnabled(): Boolean {
        return SettingsUtils.getFormUpdateMode(
            application,
            generalSettings
        ) == FormUpdateMode.MATCH_EXACTLY
    }

    fun isSyncing(): LiveData<Boolean> {
        return syncRepository.isSyncing(projectId)
    }

    fun isOutOfSync(): LiveData<Boolean> {
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

    fun syncWithServer(): LiveData<Boolean> {
        logManualSync()
        val result = MutableLiveData<Boolean>()
        scheduler.immediate(
            { formsUpdater.matchFormsWithServer(projectId) },
            { value: Boolean -> result.setValue(value) }
        )
        return result
    }

    private fun logManualSync() {
        val uri = Uri.parse(generalSettings.getString(ProjectKeys.KEY_SERVER_URL))
        val host = if (uri.host != null) uri.host else ""
        val urlHash = getMd5Hash(ByteArrayInputStream(host!!.toByteArray())) ?: ""
        analytics.logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC, "Manual", urlHash)
    }

    class Factory(
        private val formsRepository: FormsRepository,
        private val application: Application,
        private val syncRepository: SyncStatusAppState,
        private val formsUpdater: FormsUpdater,
        private val scheduler: Scheduler,
        private val generalSettings: Settings,
        private val analytics: Analytics,
        private val projectId: String
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormListViewModel(
                formsRepository,
                application,
                syncRepository,
                formsUpdater,
                scheduler,
                generalSettings,
                analytics,
                projectId
            ) as T
        }
    }

    override fun onCleared() {
        super.onCleared()
        sortingOrder.removeObserver(sortingOrderObserver)
    }
}

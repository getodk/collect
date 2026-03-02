package org.odk.collect.android.instancemanagement.send

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.analytics.AnalyticsEvents.SUBMISSION
import org.odk.collect.android.application.Collect
import org.odk.collect.android.instancemanagement.InstanceDeleter
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.utilities.InstanceAutoDeleteChecker
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class InstanceUploadViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val instanceUploader: InstanceUploader,
    private val instanceDeleter: InstanceDeleter,
    private val webCredentialsUtils: WebCredentialsUtils,
    private val propertyManager: PropertyManager,
    private val instancesRepository: InstancesRepository,
    private val formsRepository: FormsRepository,
    private val settingsProvider: SettingsProvider,
    private val instancesDataService: InstancesDataService,
    private val projectId: String,
    private val referrer: String,
    val externalUrl: String?,
    val externalUsername: String?,
    val externalPassword: String?,
    private val externalDeleteAfterUpload: Boolean? = null,
    private val defaultSuccessMessage: String,
    initUploadingStatus: String
) : ViewModel() {
    var uploadingStatus: String = initUploadingStatus
        private set

    private val _state = MutableLiveData<UploadState>(UploadState.Idle)
    val state: LiveData<UploadState> = _state

    private var uploadJob: Job? = null

    init {
        setTemporaryCredentials()
    }

    fun upload(instanceIdsToUpload: List<Long>) {
        if (_state.value !is UploadState.Idle && _state.value !is UploadState.AuthRequired) {
            return
        }
        _state.value = UploadState.Starting

        uploadJob = viewModelScope.launch(dispatcher) {
            val instancesToUpload = getInstancesToUpload(instanceIdsToUpload)
            val deviceId = propertyManager.getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID)
            val results = mutableMapOf<String, String>()

            try {
                instancesToUpload.forEachIndexed { index, instance ->
                    ensureActive()
                    _state.postValue(UploadState.Progress(index + 1, instancesToUpload.size))

                    if (externalUrl != null) {
                        Analytics.log(
                            AnalyticsEvents.INSTANCE_UPLOAD_CUSTOM_SERVER,
                            "label",
                            referrer ?: ""
                        )
                    }

                    try {
                        results[instance.dbId.toString()] = uploadInstance(instance, deviceId)

                        Analytics.log(
                            SUBMISSION,
                            "HTTP",
                            Collect.getFormIdentifierHash(
                                formsRepository,
                                instance.formId,
                                instance.formVersion
                            )
                        )
                    } catch (e: FormUploadAuthRequestedException) {
                        _state.postValue(UploadState.AuthRequired(e.authRequestingServer, results))
                        return@launch
                        // Don't add the instance that caused an auth request to the map because we want to
                        // retry. Items present in the map are considered already attempted and won't be
                        // retried.
                    } catch (e: FormUploadException) {
                        results[instance.dbId.toString()] = e.message
                    }
                }
            } finally {
                deleteInstances(results)
            }

            clearTemporaryCredentials()
            instancesDataService.update(projectId)
            _state.postValue(UploadState.Completed(results))
        }
    }

    fun cancel() {
        clearTemporaryCredentials()
        uploadJob?.cancel()
    }

    fun setUploadingStatus(uploadingStatus: String) {
        this.uploadingStatus = uploadingStatus
    }


    private fun setTemporaryCredentials() {
        if (externalUrl != null && externalUsername != null && externalPassword != null) {
            webCredentialsUtils.saveCredentials(
                externalUrl,
                externalUsername,
                externalPassword
            )
        }
    }

    private fun clearTemporaryCredentials() {
        if (externalUrl != null) {
            webCredentialsUtils.clearCredentials(externalUrl)
        }
    }

    private fun getInstancesToUpload(instanceIds: List<Long>) =
        instanceIds
            .mapNotNull { instancesRepository.get(it) }
            .sortedBy { it.finalizationDate }

    private fun uploadInstance(instance: Instance, deviceId: String): String {
        val message = instanceUploader.uploadOneSubmission(instance, deviceId, externalUrl)
            ?: defaultSuccessMessage

        return message
    }

    // Delete instances that were successfully sent and that need to be deleted
    // either because app-level auto-delete is enabled or because the form
    // specifies it.
    private fun deleteInstances(results: Map<String, String>) {
        val isFormAutoDeleteOptionEnabled =
            externalDeleteAfterUpload
                ?: settingsProvider
                    .getUnprotectedSettings()
                    .getBoolean(ProjectKeys.KEY_DELETE_AFTER_SEND)

        val idsToDelete = results.keys
            .mapNotNull { id -> instancesRepository.get(id.toLong()) }
            .filter { it.status == Instance.STATUS_SUBMITTED }
            .filter {
                InstanceAutoDeleteChecker.shouldInstanceBeDeleted(
                    formsRepository,
                    isFormAutoDeleteOptionEnabled,
                    it
                )
            }
            .map { it.dbId }
            .toTypedArray()

        instanceDeleter.delete(idsToDelete)
    }
}

sealed interface UploadState {
    object Idle : UploadState

    object Starting : UploadState

    data class Progress(val current: Int, val total: Int) : UploadState

    data class Completed(val results: Map<String, String>) : UploadState

    data class AuthRequired(val server: Uri, val results: Map<String, String>) : UploadState
}

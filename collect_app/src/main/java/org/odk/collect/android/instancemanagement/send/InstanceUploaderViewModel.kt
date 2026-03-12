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
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.instances.InstancesRepository

class InstanceUploadViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val webCredentialsUtils: WebCredentialsUtils,
    private val instancesRepository: InstancesRepository,
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
            val uploadResults = instancesDataService.sendInstances(
                projectId,
                instancesToUpload,
                referrer,
                externalUrl,
                true,
                externalDeleteAfterUpload,
                defaultSuccessMessage,
                { coroutineContext.ensureActive() }
            ) { current, total ->
                _state.postValue(UploadState.Progress(current, total))
            }

            uploadResults
                .filterIsInstance<InstanceUploadResult.Error>()
                .map { it.exception }
                .filterIsInstance<FormUploadAuthRequestedException>()
                .firstOrNull()
                ?.let {
                    _state.postValue(UploadState.AuthRequired(it.authRequestingServer, uploadResults))
                    return@launch
                }

            clearTemporaryCredentials()
            instancesDataService.update(projectId)
            _state.postValue(UploadState.Completed(uploadResults))
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
}

sealed interface UploadState {
    object Idle : UploadState

    object Starting : UploadState

    data class Progress(val current: Int, val total: Int) : UploadState

    data class Completed(val uploadResults: List<InstanceUploadResult>) : UploadState

    data class AuthRequired(val server: Uri, val uploadResults: List<InstanceUploadResult>) : UploadState
}

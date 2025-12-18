package org.odk.collect.android.instancemanagement.send

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
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
import org.odk.collect.openrosa.http.OpenRosaHttpInterface
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class InstanceUploadViewModel(
    private val httpInterface: OpenRosaHttpInterface,
    private val webCredentialsUtils: WebCredentialsUtils,
    private val propertyManager: PropertyManager,
    private val instancesRepository: InstancesRepository,
    private val formsRepository: FormsRepository,
    private val settingsProvider: SettingsProvider,
    private val instancesDataService: InstancesDataService,
    private val projectId: String,
    private val defaultSuccessMessage: String
) : ViewModel() {
    private var completeDestinationUrl: String? = null
    private var referrer: String? = null
    private var customUsername: String? = null
    private var customPassword: String? = null
    private var deleteInstanceAfterSubmission: Boolean? = null

    private val _state = MutableLiveData<UploadState>(UploadState.Idle)
    val state: LiveData<UploadState> = _state

    private var uploadJob: Job? = null

    fun upload(instanceIdsToUpload: List<Long>) {
        if (_state.value !is UploadState.Idle && _state.value !is UploadState.AuthRequired) {
            return
        }
        _state.value = UploadState.Starting

        uploadJob = viewModelScope.launch(Dispatchers.IO) {
            val uploader = InstanceUploader(
                httpInterface,
                webCredentialsUtils,
                settingsProvider.getUnprotectedSettings(),
                instancesRepository
            )

            val instancesToUpload = instanceIdsToUpload
                .mapNotNull { instancesRepository.get(it) }
                .sortedBy { it.finalizationDate }

            val deviceId = propertyManager
                .getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID)

            val results = mutableMapOf<String, String>()

            try {
                instancesToUpload.forEachIndexed { index, instance ->
                    ensureActive()
                    _state.postValue(UploadState.Progress(index + 1, instancesToUpload.size))

                    if (completeDestinationUrl != null) {
                        Analytics.log(
                            AnalyticsEvents.INSTANCE_UPLOAD_CUSTOM_SERVER,
                            "label",
                            referrer ?: ""
                        )
                    }

                    try {
                        val destinationUrl = uploader.getUrlToSubmitTo(
                            instance,
                            deviceId,
                            completeDestinationUrl,
                            null
                        )

                        val message = uploader.uploadOneSubmission(instance, destinationUrl)
                            ?: defaultSuccessMessage

                        results[instance.dbId.toString()] = message

                        Analytics.log(
                            SUBMISSION,
                            "HTTP",
                            Collect.getFormIdentifierHash(
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

    fun setDeleteInstanceAfterSubmission(deleteInstanceAfterSubmission: Boolean) {
        this.deleteInstanceAfterSubmission = deleteInstanceAfterSubmission
    }

    fun setCompleteDestinationUrl(
        completeDestinationUrl: String,
        referrer: String,
        clearPreviousConfig: Boolean
    ) {
        this.completeDestinationUrl = completeDestinationUrl
        this.referrer = referrer
        if (clearPreviousConfig) {
            setTemporaryCredentials()
        }
    }

    fun setCustomCredentials(customUsername: String, customPassword: String) {
        this.customUsername = customUsername
        this.customPassword = customPassword
        setTemporaryCredentials()
    }

    private fun setTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.saveCredentials(
                completeDestinationUrl!!,
                customUsername!!,
                customPassword!!
            )
        } else {
            // In the case for anonymous logins, clear the previous credentials for that host
            webCredentialsUtils.clearCredentials(completeDestinationUrl!!)
        }
    }

    private fun clearTemporaryCredentials() {
        if (customUsername != null && customPassword != null) {
            webCredentialsUtils.clearCredentials(completeDestinationUrl!!)
        }
    }

    // Delete instances that were successfully sent and that need to be deleted
    // either because app-level auto-delete is enabled or because the form
    // specifies it.
    private fun deleteInstances(results: Map<String, String>) {
        val isFormAutoDeleteOptionEnabled =
            deleteInstanceAfterSubmission
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

        val instanceDeleter = InstanceDeleter(instancesRepository, formsRepository)
        instanceDeleter.delete(idsToDelete)
    }

    fun cancel() {
        clearTemporaryCredentials()
        uploadJob?.cancel()
    }
}

sealed interface UploadState {
    object Idle : UploadState

    object Starting : UploadState

    data class Progress(val current: Int, val total: Int) : UploadState

    data class Completed(val results: Map<String, String>) : UploadState

    data class AuthRequired(val server: Uri, val results: Map<String, String>) : UploadState
}

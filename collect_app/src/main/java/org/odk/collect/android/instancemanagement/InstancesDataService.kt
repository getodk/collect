package org.odk.collect.android.instancemanagement

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.application.Collect
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.formentry.FormEntryUseCases
import org.odk.collect.android.formmanagement.CollectFormEntryControllerFactory
import org.odk.collect.android.instancemanagement.autosend.FormAutoSendMode
import org.odk.collect.android.instancemanagement.autosend.InstanceAutoSendFetcher
import org.odk.collect.android.instancemanagement.autosend.getAutoSendMode
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.utilities.ExternalizableFormDefCache
import org.odk.collect.android.utilities.FormsUploadResultInterpreter
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.metadata.PropertyManager
import java.io.File

class InstancesDataService(
    private val appState: AppState,
    private val instanceSubmitScheduler: InstanceSubmitScheduler,
    private val projectDependencyProviderFactory: ProjectDependencyProviderFactory,
    private val notifier: Notifier,
    private val propertyManager: PropertyManager,
    private val httpInterface: OpenRosaHttpInterface,
    private val onUpdate: () -> Unit
) {
    val editableCount: LiveData<Int> = appState.getLive(EDITABLE_COUNT_KEY, 0)
    val sendableCount: LiveData<Int> = appState.getLive(SENDABLE_COUNT_KEY, 0)
    val sentCount: LiveData<Int> = appState.getLive(SENT_COUNT_KEY, 0)

    fun getInstances(projectId: String): Flow<List<Instance>> {
        return appState.getFlow("instances:$projectId", emptyList())
    }

    fun update(projectId: String) {
        val projectDependencyProvider = projectDependencyProviderFactory.create(projectId)
        val instancesRepository = projectDependencyProvider.instancesRepository

        val sendableInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )
        val sentInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_SUBMITTED,
            Instance.STATUS_SUBMISSION_FAILED
        )
        val editableInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID
        )

        appState.setLive(EDITABLE_COUNT_KEY, editableInstances)
        appState.setLive(SENDABLE_COUNT_KEY, sendableInstances)
        appState.setLive(SENT_COUNT_KEY, sentInstances)
        appState.setFlow("instances:$projectId", instancesRepository.all)

        onUpdate()
    }

    fun finalizeAllDrafts(projectId: String): FinalizeAllResult {
        val projectDependencyProvider = projectDependencyProviderFactory.create(projectId)
        val instancesRepository = projectDependencyProvider.instancesRepository
        val formsRepository = projectDependencyProvider.formsRepository
        val storagePathProvider = projectDependencyProvider.storagePathProvider
        val savepointsRepository = projectDependencyProvider.savepointsRepository
        val entitiesRepository = projectDependencyProvider.entitiesRepository

        val projectRootDir = File(storagePathProvider.getProjectRootDirPath())

        val instances = instancesRepository.getAllByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID
        )

        val result = instances.fold(FinalizeAllResult(0, 0, false)) { result, instance ->
            val formDefAndForm = FormEntryUseCases.loadFormDef(
                instance,
                formsRepository,
                projectRootDir,
                ExternalizableFormDefCache()
            )

            if (formDefAndForm == null) {
                result.copy(failureCount = result.failureCount + 1)
            } else {
                val (formDef, form) = formDefAndForm

                val formMediaDir = File(form.formMediaPath)
                val formEntryController =
                    CollectFormEntryControllerFactory().create(formDef, formMediaDir)
                val formController =
                    FormEntryUseCases.loadDraft(form, instance, formEntryController)
                if (formController == null) {
                    result.copy(failureCount = result.failureCount + 1)
                } else {
                    val savePoint = savepointsRepository.get(form.dbId, instance.dbId)
                    val needsEncrypted = form.basE64RSAPublicKey != null
                    val newResult = if (savePoint != null) {
                        Analytics.log(AnalyticsEvents.BULK_FINALIZE_SAVE_POINT)
                        result.copy(
                            failureCount = result.failureCount + 1,
                            unsupportedInstances = true
                        )
                    } else if (needsEncrypted) {
                        Analytics.log(AnalyticsEvents.BULK_FINALIZE_ENCRYPTED_FORM)
                        result.copy(
                            failureCount = result.failureCount + 1,
                            unsupportedInstances = true
                        )
                    } else {
                        val finalizedInstance = FormEntryUseCases.finalizeDraft(
                            formController,
                            instancesRepository,
                            entitiesRepository
                        )

                        if (finalizedInstance == null) {
                            result.copy(failureCount = result.failureCount + 1)
                        } else {
                            instanceFinalized(projectId, form)
                            result
                        }
                    }

                    Collect.getInstance().externalDataManager?.close()
                    newResult
                }
            }
        }

        update(projectId)

        return result.copy(successCount = instances.size - result.failureCount)
    }

    fun deleteInstances(projectId: String, instanceIds: LongArray): Boolean {
        val projectDependencyProvider = projectDependencyProviderFactory.create(projectId)
        val instancesRepository = projectDependencyProvider.instancesRepository
        val formsRepository = projectDependencyProvider.formsRepository

        return projectDependencyProvider.instancesLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                instanceIds.forEach { instanceId ->
                    InstanceDeleter(
                        instancesRepository,
                        formsRepository
                    ).delete(
                        instanceId
                    )
                }

                update(projectId)
                true
            } else {
                false
            }
        }
    }

    fun deleteAll(projectId: String): Boolean {
        val projectDependencyProvider =
            projectDependencyProviderFactory.create(projectId)
        val instancesRepository = projectDependencyProvider.instancesRepository

        return projectDependencyProvider.instancesLock.withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                instancesRepository.deleteAll()
                update(projectId)
                true
            } else {
                false
            }
        }
    }

    fun sendInstances(projectId: String, formAutoSend: Boolean = false): Boolean {
        val projectDependencyProvider =
            projectDependencyProviderFactory.create(projectId)

        val instanceSubmitter = InstanceSubmitter(
            projectDependencyProvider.formsRepository,
            projectDependencyProvider.generalSettings,
            propertyManager,
            httpInterface,
            projectDependencyProvider.instancesRepository
        )

        return projectDependencyProvider.changeLockProvider.getInstanceLock(
            projectDependencyProvider.projectId
        ).withLock { acquiredLock: Boolean ->
            if (acquiredLock) {
                val toUpload = InstanceAutoSendFetcher.getInstancesToAutoSend(
                    projectDependencyProvider.instancesRepository,
                    projectDependencyProvider.formsRepository,
                    formAutoSend
                )

                if (toUpload.isNotEmpty()) {
                    val results = instanceSubmitter.submitInstances(toUpload)
                    notifier.onSubmission(results, projectDependencyProvider.projectId)
                    update(projectId)

                    FormsUploadResultInterpreter.allFormsUploadedSuccessfully(results)
                } else {
                    true
                }
            } else {
                false
            }
        }
    }

    fun instanceFinalized(projectId: String, form: Form) {
        if (form.getAutoSendMode() == FormAutoSendMode.FORCED) {
            instanceSubmitScheduler.scheduleFormAutoSend(projectId)
        } else {
            instanceSubmitScheduler.scheduleAutoSend(projectId)
        }
    }

    companion object {
        private const val EDITABLE_COUNT_KEY = "instancesEditableCount"
        private const val SENDABLE_COUNT_KEY = "instancesSendableCount"
        private const val SENT_COUNT_KEY = "instancesSentCount"
    }
}

data class FinalizeAllResult(
    val successCount: Int,
    val failureCount: Int,
    val unsupportedInstances: Boolean
)

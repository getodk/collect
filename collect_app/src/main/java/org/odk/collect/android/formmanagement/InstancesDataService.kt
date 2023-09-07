package org.odk.collect.android.formmanagement

import android.content.Context
import androidx.lifecycle.LiveData
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.androidshared.data.getState
import org.odk.collect.forms.instances.Instance

class InstancesDataService(
    private val context: Context,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val currentProjectProvider: CurrentProjectProvider
) {
    private val appState = context.getState()

    val editableCount: LiveData<Int> = appState.getLive(EDITABLE_COUNT_KEY, 0)
    val sendableCount: LiveData<Int> = appState.getLive(SENDABLE_COUNT_KEY, 0)
    val sentCount: LiveData<Int> = appState.getLive(SENT_COUNT_KEY, 0)

    fun update() {
        val instancesRepository = instancesRepositoryProvider.get()

        val sendableInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )
        val sentInstances = instancesRepository.getCountByStatus(
            Instance.STATUS_SUBMITTED,
            Instance.STATUS_SUBMISSION_FAILED
        )

        val editableInstances = instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE)

        appState.setLive(EDITABLE_COUNT_KEY, editableInstances)
        appState.setLive(SENDABLE_COUNT_KEY, sendableInstances)
        appState.setLive(SENT_COUNT_KEY, sentInstances)

        context.contentResolver.notifyChange(
            InstancesContract.getUri(currentProjectProvider.getCurrentProject().uuid),
            null
        )
    }

    companion object {
        private const val EDITABLE_COUNT_KEY = "instancesEditableCount"
        private const val SENDABLE_COUNT_KEY = "instancesSendableCount"
        private const val SENT_COUNT_KEY = "instancesSentCount"
    }
}

package org.odk.collect.android.formmanagement

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.provider.InstanceProviderAPI
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import javax.inject.Singleton

/**
 * Stores reactive state of instances. This (as a singleton) can be read or updated by
 * different parts of the app without needing reactive data in the [InstancesRepository].
 */
@Singleton
class InstancesAppState(
    private val context: Context,
    private val instancesRepositoryProvider: InstancesRepositoryProvider
) {

    private val _unsent = MutableLiveData(0)
    val unsentCount: LiveData<Int> = _unsent

    private val _finalized = MutableLiveData(0)
    val finalizedCount: LiveData<Int> = _finalized

    private val _sent = MutableLiveData(0)
    val sentCount: LiveData<Int> = _sent

    fun update() {
        val instancesRepository = instancesRepositoryProvider.get()
        val finalizedInstances = instancesRepository.getCountByStatus(Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED)
        val sentInstances = instancesRepository.getCountByStatus(Instance.STATUS_SUBMITTED)
        val unsentInstances = instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED)

        _finalized.postValue(finalizedInstances)
        _sent.postValue(sentInstances)
        _unsent.postValue(unsentInstances)

        context.contentResolver.notifyChange(InstanceProviderAPI.CONTENT_URI, null)
    }
}

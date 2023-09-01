package org.odk.collect.android.instancemanagement.send

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import java.util.function.Supplier

class ReadyToSendViewModel(
    private val instancesRepository: InstancesRepository,
    scheduler: Scheduler,
    private val clock: Supplier<Long>
) : ViewModel() {
    private val _data = MutableLiveData<Data>()
    val data: LiveData<Data> = _data

    init {
        scheduler.immediate(
            background = {
                val sentInstances = instancesRepository.getAllByStatus(Instance.STATUS_SUBMITTED)
                val numberOfSentInstances = sentInstances.size
                val numberOfInstancesReadyToSend = instancesRepository.getCountByStatus(
                    Instance.STATUS_COMPLETE,
                    Instance.STATUS_SUBMISSION_FAILED
                )
                val lastInstanceSentTimeMillis = if (sentInstances.isNotEmpty()) {
                    val lastSentInstance = sentInstances.maxBy { instance -> instance.lastStatusChangeDate }
                    clock.get() - lastSentInstance.lastStatusChangeDate
                } else {
                    0
                }
                Data(numberOfInstancesReadyToSend, numberOfSentInstances, lastInstanceSentTimeMillis)
            },
            foreground = {
                _data.value = it
            }
        )
    }

    open class Factory(
        private val instancesRepository: InstancesRepository,
        private val scheduler: Scheduler,
        private val clock: Supplier<Long>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReadyToSendViewModel(instancesRepository, scheduler, clock) as T
        }
    }

    data class Data(
        val numberOfInstancesReadyToSend: Int,
        val numberOfSentInstances: Int,
        val lastInstanceSentTimeMillis: Long
    )
}

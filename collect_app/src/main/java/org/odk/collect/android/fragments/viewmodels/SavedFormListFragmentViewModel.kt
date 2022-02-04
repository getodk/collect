package org.odk.collect.android.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.instancemanagement.InstanceDeleter
import org.odk.collect.async.Scheduler
import timber.log.Timber

class SavedFormListFragmentViewModel(
    private val scheduler: Scheduler,
    private val instanceDeleter: InstanceDeleter
) : ViewModel() {

    fun deleteInstances(ids: List<Long>): LiveData<DeleteNotifier> {
        val result = MutableLiveData<DeleteNotifier>()

        scheduler.immediate(
            {
                var deleted = 0
                val toDeleteCount = ids.size

                for (id in ids) {
                    try {
                        instanceDeleter.delete(id)
                        deleted++
                        result.postValue(DeleteNotifier(deleted, toDeleteCount, false))
                        Thread.sleep(3000)
                    } catch (e: Exception) {
                        Timber.e(
                            "Exception during delete of: %s exception: %s",
                            id.toString(),
                            e.toString()
                        )
                    }
                }

                return@immediate DeleteNotifier(deleted, toDeleteCount, true)
            },
            {
                result.postValue(it)
            }
        )

        return result
    }

    data class DeleteNotifier(
        val progress: Int,
        val total: Int,
        val complete: Boolean
    )

    class Factory(
        private val scheduler: Scheduler,
        private val instanceDeleter: InstanceDeleter
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SavedFormListFragmentViewModel(scheduler, instanceDeleter) as T
        }
    }
}

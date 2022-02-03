package org.odk.collect.android.preferences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.utilities.ProjectResetter
import org.odk.collect.async.Scheduler

class ProjectManagementPreferencesFragmentViewModel(
    private val scheduler: Scheduler,
    private val projectResetter: ProjectResetter
) : ViewModel() {

    fun reset(resetActions: List<Int>): LiveData<Pair<List<Int>, List<Int>>> {
        val result = MutableLiveData<Pair<List<Int>, List<Int>>>()
        scheduler.immediate(
            {
                return@immediate projectResetter.reset(resetActions)
            },
            { failedResetActions ->
                result.postValue(Pair(resetActions, failedResetActions))
            }
        )

        return result
    }

    class Factory(
        private val scheduler: Scheduler,
        private val projectResetter: ProjectResetter
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProjectManagementPreferencesFragmentViewModel(scheduler, projectResetter) as T
        }
    }
}

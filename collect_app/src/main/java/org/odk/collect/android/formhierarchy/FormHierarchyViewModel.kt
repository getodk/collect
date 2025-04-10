package org.odk.collect.android.formhierarchy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.instance.TreeReference
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.androidshared.async.TrackableWorker
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.async.Scheduler

class FormHierarchyViewModel(
    scheduler: Scheduler,
    private val projectId: String,
    private val instancesDataService: InstancesDataService
) : ViewModel() {
    private val trackableWorker = TrackableWorker(scheduler)
    val isCloning: LiveData<Boolean> = trackableWorker.isWorking

    var contextGroupRef: TreeReference? = null
    var screenIndex: FormIndex? = null
    var repeatGroupPickerIndex: FormIndex? = null
    var currentIndex: FormIndex? = null
    var elementsToDisplay: List<HierarchyItem>? = null
    var startIndex: FormIndex? = null

    fun shouldShowRepeatGroupPicker() = repeatGroupPickerIndex != null

    fun editInstance(formController: FormController): LiveData<Consumable<Long>> {
        val result = MutableLiveData<Consumable<Long>>()

        trackableWorker.immediate(
            background = {
                instancesDataService.clone(formController.getInstanceFile(), projectId)
            },
            foreground = { dbId ->
                if (dbId != null) {
                    result.value = Consumable(dbId)
                }
            }
        )

        return result
    }

    class Factory(
        private val scheduler: Scheduler,
        private val projectId: String,
        private val instancesDataService: InstancesDataService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormHierarchyViewModel(
                scheduler,
                projectId,
                instancesDataService
            ) as T
        }
    }
}

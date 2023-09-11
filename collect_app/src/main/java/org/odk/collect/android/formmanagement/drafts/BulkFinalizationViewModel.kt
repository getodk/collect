package org.odk.collect.android.formmanagement.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.formmanagement.InstancesDataService
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.async.Scheduler

class BulkFinalizationViewModel(
    private val scheduler: Scheduler,
    private val instancesDataService: InstancesDataService
) {
    private val _finalizedForms = MutableLiveData<Consumable<Int>>()
    val finalizedForms: LiveData<Consumable<Int>> = _finalizedForms

    private val _isFinalizing = MutableNonNullLiveData(false)
    val isFinalizing: NonNullLiveData<Boolean> = _isFinalizing

    fun finalizeAllDrafts() {
        _isFinalizing.value = true

        scheduler.immediate(
            background = {
                instancesDataService.finalizeAllDrafts()
            },
            foreground = {
                _isFinalizing.value = false
                _finalizedForms.value = Consumable(it)
            }
        )
    }
}

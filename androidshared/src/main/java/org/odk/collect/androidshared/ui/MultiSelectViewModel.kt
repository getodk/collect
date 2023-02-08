package org.odk.collect.androidshared.ui

import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData

class MultiSelectViewModel : ViewModel() {

    private val selected = MutableNonNullLiveData(emptySet<Long>())

    fun select(item: Long) {
        selected.value = selected.value + item
    }

    fun getSelected(): NonNullLiveData<Set<Long>> {
        return selected
    }

    fun unselect(item: Long) {
        selected.value = selected.value - item
    }

    fun unselectAll() {
        selected.value = emptySet()
    }
}

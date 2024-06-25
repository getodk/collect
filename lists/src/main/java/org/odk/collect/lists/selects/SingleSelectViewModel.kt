package org.odk.collect.lists.selects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import org.odk.collect.androidshared.livedata.LiveDataUtils

class SingleSelectViewModel(data: LiveData<List<SelectItem<*>>>) : ViewModel() {

    private val _selected = MutableLiveData<String?>(null)
    private val selected = LiveDataUtils.zip(_selected, data).map { (selected, data) ->
        if (selected != null && data.any { it.id == selected }) {
            selected
        } else {
            data.find { it.selected }?.id
        }
    }

    fun getSelected(): LiveData<String?> {
        return selected
    }

    fun select(item: String) {
        _selected.value = item
    }

    fun clear() {
        _selected.value = null
    }
}

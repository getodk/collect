package org.odk.collect.lists.selects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import org.odk.collect.androidshared.livedata.LiveDataUtils

class SingleSelectViewModel(
    selected: String?,
    data: LiveData<List<SelectItem<*>>>
) : ViewModel() {

    private val _selected = MutableLiveData<String?>(selected)
    private val selected = LiveDataUtils.combine(_selected, data).map { (selected, data) ->
        if (selected != null && data != null) {
            selected.takeIf { id -> data.any { it.id == id } }
        } else {
            null
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

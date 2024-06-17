package org.odk.collect.lists.selects

import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.CreationExtras
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData

/**
 * A `ViewModel` for holding state around selected items (identified by `Long` ids). This can
 * optionally also handle the data for the list which allows the `ViewModel` to perform a select
 * all and determine whether all items are selected or not.
 */
class MultiSelectViewModel<T>(
    private val data: LiveData<List<SelectItem<T>>> = MutableLiveData(emptyList())
) : ViewModel() {

    private val selected = MutableNonNullLiveData(emptySet<String>())
    private val isAllSelected = LiveDataUtils.zip(data, selected).map { (data, selected) ->
        data.isNotEmpty() && data.size == selected.size
    }

    fun getData(): LiveData<List<SelectItem<T>>> {
        return data
    }

    fun select(item: String) {
        updateSelected(selected.value + item)
    }

    fun getSelected(): NonNullLiveData<Set<String>> {
        return selected
    }

    fun unselect(item: String) {
        updateSelected(selected.value - item)
    }

    fun unselectAll() {
        updateSelected(emptySet())
    }

    fun selectAll() {
        updateSelected(data.value?.map { it.id }?.toSet() ?: emptySet())
    }

    fun isAllSelected(): LiveData<Boolean> {
        return isAllSelected
    }

    fun toggle(item: String) {
        if (selected.value.contains(item)) {
            unselect(item)
        } else {
            select(item)
        }
    }

    private fun updateSelected(new: Set<String>) {
        selected.value = new
    }

    class Factory<T>(private val data: LiveData<List<SelectItem<T>>> = MutableLiveData(emptyList())) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>, extras: CreationExtras): VM {
            return MultiSelectViewModel(data) as VM
        }
    }
}

fun updateSelectAll(button: Button, itemCount: Int, selectedCount: Int): Boolean {
    val allSelected = itemCount > 0 && selectedCount == itemCount

    if (allSelected) {
        button.setText(org.odk.collect.strings.R.string.clear_all)
    } else {
        button.setText(org.odk.collect.strings.R.string.select_all)
    }

    return allSelected
}

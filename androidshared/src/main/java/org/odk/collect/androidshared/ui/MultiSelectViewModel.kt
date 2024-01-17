package org.odk.collect.androidshared.ui

import android.widget.Button
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData

class MultiSelectViewModel : ViewModel() {

    var data = emptySet<Long>()
        set(value) {
            field = value
            updateAllSelected()
        }

    private val selected = MutableNonNullLiveData(emptySet<Long>())
    private val isAllSelected = MutableLiveData(false)

    fun select(item: Long) {
        updateSelected(selected.value + item)
    }

    fun getSelected(): NonNullLiveData<Set<Long>> {
        return selected
    }

    fun unselect(item: Long) {
        updateSelected(selected.value - item)
    }

    fun unselectAll() {
        updateSelected(emptySet())
    }

    fun selectAll() {
        updateSelected(data)
    }

    fun isAllSelected(): LiveData<Boolean> {
        return isAllSelected
    }

    fun toggle(item: Long) {
        if (selected.value.contains(item)) {
            unselect(item)
        } else {
            select(item)
        }
    }

    private fun updateSelected(new: Set<Long>) {
        selected.value = new
        updateAllSelected()
    }

    private fun updateAllSelected() {
        isAllSelected.value = data.isNotEmpty() && data.size == selected.value.size
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

fun setupControls(
    controls: MultiSelectControlsView,
    actionText: String,
    multiSelectViewModel: MultiSelectViewModel,
    lifecycleOwner: LifecycleOwner,
    onAction: (Set<Long>) -> Unit
) {
    controls.actionText = actionText

    multiSelectViewModel.getSelected().observe(lifecycleOwner) {
        controls.selected = it
    }

    multiSelectViewModel.isAllSelected().observe(lifecycleOwner) {
        controls.isAllSelected = it
    }

    controls.listener = object : MultiSelectControlsView.Listener {
        override fun onSelectAll() {
            multiSelectViewModel.selectAll()
        }

        override fun onClearAll() {
            multiSelectViewModel.unselectAll()
        }

        override fun onAction(selected: Set<Long>) {
            onAction(selected)
        }
    }
}

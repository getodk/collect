package org.odk.collect.androidshared.ui

import androidx.lifecycle.ViewModel

class MultiSelectViewModel : ViewModel() {

    private val selected = mutableSetOf<Long>()

    fun select(item: Long) {
        selected.add(item)
    }

    fun getSelected(): Set<Long> {
        return selected
    }

    fun unselect(item: Long) {
        selected.remove(item)
    }
}

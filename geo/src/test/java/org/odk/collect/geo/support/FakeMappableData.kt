package org.odk.collect.geo.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.geo.items.MappableData
import org.odk.collect.geo.items.MappableItem

class FakeMappableData(items: List<MappableItem>) : MappableData {

    private val _items = MutableLiveData(items)
    private val _isLoading = MutableNonNullLiveData(false)
    var isLoading
        get() = _isLoading.value
        set(value) { _isLoading.value = value }

    override fun getMappableItems(): LiveData<List<MappableItem>> {
        return _items
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return _isLoading
    }
}

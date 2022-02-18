package org.odk.collect.androidtest

import androidx.lifecycle.LiveData

class LiveDataTester {

    private val owner = FakeLifecycleOwner()

    fun <T> activate(liveData: LiveData<T>): LiveData<T> {
        liveData.observe(owner) { }
        return liveData
    }

    fun teardown() {
        owner.destroy()
    }
}

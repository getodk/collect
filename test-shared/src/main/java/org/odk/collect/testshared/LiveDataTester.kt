package org.odk.collect.testshared

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

class LiveDataTester {

    private val owner = FakeLifecycleOwner()

    fun <T> activate(liveData: LiveData<T>): LiveData<T> {
        liveData.observe(owner, Observer { })
        return liveData
    }

    fun teardown() {
        owner.destroy()
    }
}

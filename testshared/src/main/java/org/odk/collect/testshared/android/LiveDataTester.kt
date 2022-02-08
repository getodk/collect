package org.odk.collect.testshared.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.odk.collect.testshared.FakeLifecycleOwner

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

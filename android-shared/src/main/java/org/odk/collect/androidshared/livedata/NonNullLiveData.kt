package org.odk.collect.androidshared.livedata

import androidx.lifecycle.LiveData

/**
 * Allows creating LiveData values that can be used without null checks
 */
abstract class NonNullLiveData<T : Any>(value: T) : LiveData<T>(value) {

    override fun getValue(): T {
        return super.getValue() as T
    }
}

class MutableNonNullLiveData<T : Any>(value: T) : NonNullLiveData<T>(value) {

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }
}

package org.odk.collect.androidshared.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

object LiveDataExt {
    fun <T, U> LiveData<T>.zip(other: LiveData<U>): LiveData<Pair<T, U>> {
        return LiveDataUtils.zip(this, other)
    }

    fun <T, U> LiveData<T>.runningFold(initial: U, operation: (U, T) -> U): LiveData<U> {
        val mediator = MediatorLiveData<U>()

        var accum = initial
        mediator.addSource(this) {
            accum = operation(accum, it)
            mediator.value = accum
        }

        return mediator
    }

    /**
     * Returns a [LiveData] where each value is a [Pair] made up of the latest value and the
     * previous value.
    */
    fun <T : Any?> LiveData<T>.withLast(): LiveData<Pair<T?, T?>> {
        return this.runningFold(Pair(null, null) as Pair<T?, T?>) { last, current ->
            Pair(last.second, current)
        }
    }
}

package org.odk.collect.androidshared.livedata

import androidx.lifecycle.LiveData

object LiveDataExt {
    fun <T, U> LiveData<T>.zip(other: LiveData<U>): LiveData<Pair<T, U>> {
        return LiveDataUtils.zip(this, other)
    }
}

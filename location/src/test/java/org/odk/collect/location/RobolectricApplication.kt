package org.odk.collect.location

import android.app.Application
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.data.StateStore

class RobolectricApplication : Application(), StateStore {

    private val appState = AppState()

    override fun getState(): AppState {
        return appState
    }
}

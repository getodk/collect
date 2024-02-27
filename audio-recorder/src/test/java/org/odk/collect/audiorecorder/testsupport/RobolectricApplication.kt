package org.odk.collect.audiorecorder.testsupport

import android.app.Application
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.androidshared.data.StateStore
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponent
import org.odk.collect.audiorecorder.AudioRecorderDependencyComponentProvider
import org.odk.collect.audiorecorder.AudioRecorderDependencyModule
import org.odk.collect.audiorecorder.DaggerAudioRecorderDependencyComponent

/**
 * Used as the Application in tests in in the `test/src` root. This is setup in `robolectric.properties`
 */
internal class RobolectricApplication : Application(), AudioRecorderDependencyComponentProvider, StateStore {

    override lateinit var audioRecorderDependencyComponent: AudioRecorderDependencyComponent

    private val appState = AppState()

    override fun onCreate() {
        super.onCreate()
        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
            .application(this)
            .build()
    }

    fun setupDependencies(dependencyModule: AudioRecorderDependencyModule) {
        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
            .dependencyModule(dependencyModule)
            .application(this)
            .build()
    }

    override fun getState(): AppState {
        return appState
    }
}

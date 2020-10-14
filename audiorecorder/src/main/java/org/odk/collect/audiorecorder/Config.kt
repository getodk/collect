package org.odk.collect.audiorecorder

import android.app.Activity
import android.app.Application
import android.media.MediaRecorder
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.audiorecorder.recording.AudioRecorderActivity
import org.odk.collect.audiorecorder.recording.MediaRecorderRecorder
import org.odk.collect.audiorecorder.recording.RealMediaRecorderWrapper
import org.odk.collect.audiorecorder.recording.Recorder

private var _component: AudioRecorderDependencyComponent? = null

internal fun setAudioRecorderDependencyComponent(component: AudioRecorderDependencyComponent) {
    _component = component
}

internal fun Activity.getComponent(): AudioRecorderDependencyComponent {
    return _component.let {
        if (it == null) {
            val newComponent = DaggerAudioRecorderDependencyComponent.builder()
                    .application(application)
                    .build()

            setAudioRecorderDependencyComponent(newComponent)
            newComponent
        } else {
            it
        }
    }
}

@Component(modules = [AudioRecorderDependencyModule::class])
internal interface AudioRecorderDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun dependencyModule(audioRecorderDependencyModule: AudioRecorderDependencyModule): Builder

        fun build(): AudioRecorderDependencyComponent
    }

    fun inject(activity: AudioRecorderActivity)
}

@Module
internal open class AudioRecorderDependencyModule {

    @Provides
    open fun providesRecorder(application: Application): Recorder {
        return MediaRecorderRecorder(application.cacheDir) { RealMediaRecorderWrapper(MediaRecorder()) }
    }
}
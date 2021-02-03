package org.odk.collect.audiorecorder

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import org.odk.collect.async.CoroutineScheduler
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.mediarecorder.AACRecordingResource
import org.odk.collect.audiorecorder.mediarecorder.AMRRecordingResource
import org.odk.collect.audiorecorder.recorder.Output
import org.odk.collect.audiorecorder.recorder.Recorder
import org.odk.collect.audiorecorder.recorder.RecordingResourceRecorder
import org.odk.collect.audiorecorder.recording.AudioRecorderFactory
import org.odk.collect.audiorecorder.recording.internal.AudioRecorderService
import org.odk.collect.audiorecorder.recording.internal.RecordingRepository
import java.io.File
import javax.inject.Singleton

private var _component: AudioRecorderDependencyComponent? = null

internal fun Context.getComponent(): AudioRecorderDependencyComponent {
    return _component.let {
        if (it == null && applicationContext is RobolectricApplication) {
            throw IllegalStateException("Dependencies not specified!")
        }

        if (it == null) {
            val newComponent = DaggerAudioRecorderDependencyComponent.builder()
                .application(applicationContext as Application)
                .build()

            _component = newComponent
            newComponent
        } else {
            it
        }
    }
}

@Component(modules = [AudioRecorderDependencyModule::class])
@Singleton
internal interface AudioRecorderDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun dependencyModule(audioRecorderDependencyModule: AudioRecorderDependencyModule): Builder

        fun build(): AudioRecorderDependencyComponent
    }

    fun inject(activity: AudioRecorderService)
    fun inject(activity: AudioRecorderFactory)

    fun recordingRepository(): RecordingRepository
}

@Module
internal open class AudioRecorderDependencyModule {

    @Provides
    open fun providesCacheDir(application: Application): File {
        val externalFilesDir = application.getExternalFilesDir(null)
        return File(externalFilesDir, "recordings").also { it.mkdirs() }
    }

    @Provides
    open fun providesRecorder(cacheDir: File): Recorder {
        return RecordingResourceRecorder(cacheDir) { output ->
            when (output) {
                Output.AMR -> {
                    AMRRecordingResource(MediaRecorder(), android.os.Build.VERSION.SDK_INT)
                }

                Output.AAC -> {
                    AACRecordingResource(MediaRecorder(), android.os.Build.VERSION.SDK_INT, 64)
                }

                Output.AAC_LOW -> {
                    AACRecordingResource(MediaRecorder(), android.os.Build.VERSION.SDK_INT, 24)
                }
            }
        }
    }

    @Provides
    @Singleton
    open fun providesRecordingRepository(): RecordingRepository {
        return RecordingRepository()
    }

    @Provides
    open fun providesScheduler(application: Application): Scheduler {
        return CoroutineScheduler(Dispatchers.Main, Dispatchers.IO)
    }
}

internal fun RobolectricApplication.clearDependencies() {
    _component = null
}

internal fun RobolectricApplication.setupDependencies(module: AudioRecorderDependencyModule) {
    _component = DaggerAudioRecorderDependencyComponent.builder()
        .application(this)
        .dependencyModule(module)
        .build()
}

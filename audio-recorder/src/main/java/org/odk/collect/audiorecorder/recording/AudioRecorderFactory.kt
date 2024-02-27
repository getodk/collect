package org.odk.collect.audiorecorder.recording

import android.app.Application
import org.odk.collect.androidshared.data.getState
import org.odk.collect.audiorecorder.recording.internal.ForegroundServiceAudioRecorder
import org.odk.collect.audiorecorder.recording.internal.RecordingRepository

open class AudioRecorderFactory(private val application: Application) {

    open fun create(): AudioRecorder {
        return ForegroundServiceAudioRecorder(application, RecordingRepository(application.getState()))
    }
}

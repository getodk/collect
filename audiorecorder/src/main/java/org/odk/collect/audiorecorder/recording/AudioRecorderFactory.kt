package org.odk.collect.audiorecorder.recording

import android.app.Application
import org.odk.collect.audiorecorder.getComponent
import org.odk.collect.audiorecorder.recording.internal.ForegroundServiceAudioRecorder

open class AudioRecorderFactory(private val application: Application) {

    open fun create(): AudioRecorder {
        return ForegroundServiceAudioRecorder(application, application.getComponent().recordingRepository())
    }
}

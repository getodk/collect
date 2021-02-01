package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import org.odk.collect.audiorecorder.recorder.Output
import java.io.File
import java.io.Serializable

/**
 * Interface for a component that records audio. Can only record once session
 * at a time.
 */
abstract class AudioRecorder {
    abstract fun isRecording(): Boolean
    abstract fun getCurrentSession(): LiveData<RecordingSession?>

    abstract fun start(sessionId: Serializable, output: Output)
    abstract fun pause()
    abstract fun resume()
    abstract fun stop()

    /**
     * Stops any in progress recordings, clears recordings (returned from `getRecordings`). Should
     * be called after in-progress or finished recordings are no longer needed
     */
    abstract fun cleanUp()
}

data class RecordingSession(val id: Serializable, val file: File?, val duration: Long, val amplitude: Int, val paused: Boolean, val failedToStart: Exception?) {

    constructor(id: Serializable, file: File?, duration: Long, amplitude: Int, paused: Boolean) : this(id, file, duration, amplitude, paused, null)
}

class SetupException : java.lang.Exception()
class MicInUseException : java.lang.Exception()

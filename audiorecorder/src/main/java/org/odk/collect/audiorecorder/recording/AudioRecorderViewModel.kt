package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.audiorecorder.recorder.Output
import java.io.File

/**
 * Interface for a ViewModel that records audio. Can only record once session
 * at a time.
 */
abstract class AudioRecorderViewModel : ViewModel() {
    abstract fun isRecording(): LiveData<Boolean>
    abstract fun getCurrentSession(): LiveData<RecordingSession?>

    abstract fun start(sessionId: String, output: Output)
    abstract fun stop()

    /**
     * Stops any in progress recordings, clears recordings (returned from `getRecordings`). Should
     * be called after in-progress or finished recordings are no longer needed
     */
    abstract fun cleanUp()
}

data class RecordingSession(val id: String, val file: File?, val duration: Long)

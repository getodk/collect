package org.odk.collect.audiorecorder.recording

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.audiorecorder.recorder.Output
import java.io.File

/**
 * Interface for a ViewModel that records audio. Can only record once session
 * at a time but supports cases where multiple views can start/playback different
 * recordings through a `sessionsId` passed to `start` and `getRecording`.
 */
abstract class AudioRecorderViewModel : ViewModel() {
    abstract fun isRecording(): LiveData<Boolean>
    abstract fun getRecording(sessionId: String): LiveData<File?>
    abstract fun start(sessionId: String, output: Output)
    abstract fun stop()

    /**
     * Stops any in progress recordings, clears recordings (returned from `getRecordings`). Should
     * be called after in-progress or finished recordings are no longer needed
     */
    abstract fun cleanUp()
}

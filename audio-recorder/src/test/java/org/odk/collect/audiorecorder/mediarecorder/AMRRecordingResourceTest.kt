package org.odk.collect.audiorecorder.mediarecorder

import android.media.MediaRecorder
import org.junit.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.only
import org.mockito.Mockito.verify

class AMRRecordingResourceTest {

    /**
     * Calls to stop() while paused hang which appears to be a problem in the Android
     * framework. Resuming and the stopping does work however.
     *
     * @see [Gist with test demonstrating problem](https://gist.github.com/seadowg/e6cbab21e032fe2fcd9afb9b4f458ab9)
     * @see [Issue in Google's Android tracker](https://issuetracker.google.com/issues/178630865)
     */
    @Test
    fun whenAPI24OrHigher_whenPaused_stop_callsResumeThenStopOnMediaRecorder() {
        val mediaRecorder = mock(MediaRecorder::class.java)
        val inOrder = inOrder(mediaRecorder)
        val amrRecordingResource = AMRRecordingResource(mediaRecorder, 24)

        amrRecordingResource.pause()

        amrRecordingResource.stop()
        inOrder.verify(mediaRecorder).resume()
        inOrder.verify(mediaRecorder).stop()
    }

    /**
     * Pause/resume is not supported in API 23 and below so we need to check that we're not calling
     * [MediaRecorder.pause] in [AMRRecordingResource.stop].
     */
    @Test
    fun whenAPI23OrLower_stop_callsStopOnMediaRecorder() {
        val mediaRecorder = mock(MediaRecorder::class.java)
        val amrRecordingResource = AMRRecordingResource(mediaRecorder, 23)

        amrRecordingResource.stop()
        verify(mediaRecorder, only()).stop()
    }
}

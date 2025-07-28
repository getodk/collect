package org.odk.collect.audioclips

import android.media.MediaPlayer
import android.os.StrictMode
import java.io.IOException

/**
 * Allows a [MediaPlayer] to be interacted with from multiple threads while avoiding illegal states
 * such as multiple threads attempting to set up data sources at the same time (with interleaved
 * calls to [MediaPlayer.reset] for example).
 *
 * This also handles on-demand creation and allowing the [MediaPlayer] instance to be garbage
 * collected after calls to [release].
 */
internal class ThreadSafeMediaPlayerWrapper(
    private val mediaPlayerFactory: () -> MediaPlayer,
    private var onCompletionListener: MediaPlayer.OnCompletionListener
) {

    private var mediaPlayer: MediaPlayer? = null

    fun resetWithDataSource(uri: String): Boolean {
        synchronized(this) {
            val mediaPlayer = mediaPlayer ?: run {
                val newMediaPlayer = setupNewMediaPlayer()
                mediaPlayer = newMediaPlayer
                newMediaPlayer
            }

            return try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(uri)
                mediaPlayer.prepare()
                true
            } catch (e: IOException) {
                false
            }
        }
    }

    fun start(position: Int) {
        synchronized(this) {
            mediaPlayer?.seekTo(position)
            mediaPlayer?.start()
        }
    }

    fun pause() {
        synchronized(this) {
            mediaPlayer?.pause()
        }
    }

    fun stop() {
        synchronized(this) {
            mediaPlayer?.stop()
        }
    }

    fun seekTo(newPosition: Int) {
        synchronized(this) {
            mediaPlayer?.seekTo(newPosition)
        }
    }

    fun getPosition(): Int? {
        return mediaPlayer?.currentPosition
    }

    fun release() {
        synchronized(this) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun setupNewMediaPlayer(): MediaPlayer {
        StrictMode.noteSlowCall("MediaPlayer instantiation can be slow")

        val newMediaPlayer: MediaPlayer = mediaPlayerFactory()
        newMediaPlayer.setOnCompletionListener(onCompletionListener)
        return newMediaPlayer
    }
}

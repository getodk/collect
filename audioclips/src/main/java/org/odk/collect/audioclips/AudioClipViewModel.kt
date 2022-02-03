package org.odk.collect.audioclips

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import java.io.File
import java.io.IOException
import java.util.LinkedList
import java.util.Queue
import java.util.function.Supplier
import kotlin.jvm.Throws

class AudioClipViewModel(private val mediaPlayerFactory: Supplier<MediaPlayer>, private val scheduler: Scheduler) : ViewModel(), MediaPlayer.OnCompletionListener {

    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer: MediaPlayer
        get() {
            val mediaPlayer = _mediaPlayer ?: setupNewMediaPlayer()
            _mediaPlayer = mediaPlayer
            return mediaPlayer
        }

    private val currentlyPlaying = MutableLiveData<CurrentlyPlaying?>(null)
    private val error = MutableLiveData<Exception?>()
    private val positions: MutableMap<String, MutableLiveData<Int>?> = HashMap()
    private var positionUpdatesCancellable: Cancellable? = null

    fun play(clip: Clip) {
        val playlist = LinkedList<Clip>()
        playlist.add(clip)
        playNext(playlist)
    }

    fun playInOrder(clips: List<Clip>) {
        val playlist: Queue<Clip> = LinkedList(clips)
        playNext(playlist)
    }

    fun stop() {
        if (currentlyPlaying.value != null) {
            mediaPlayer.stop()
        }

        cleanUpAfterClip()
    }

    fun pause() {
        mediaPlayer.pause()
        val currentlyPlayingValue = currentlyPlaying.value
        if (currentlyPlayingValue != null) {
            currentlyPlaying.value = currentlyPlayingValue.paused()
        }
    }

    fun isPlaying(clipID: String): LiveData<Boolean> {
        return Transformations.map(currentlyPlaying) { value ->
            if (isCurrentPlayingClip(clipID, value)) {
                !value!!.isPaused
            } else {
                false
            }
        }
    }

    fun getPosition(clipID: String): LiveData<Int> {
        return getPositionForClip(clipID)
    }

    fun setPosition(clipID: String, newPosition: Int) {
        if (isCurrentPlayingClip(clipID, currentlyPlaying.value)) {
            mediaPlayer.seekTo(newPosition)
        }
        getPositionForClip(clipID).value = newPosition
    }

    fun background() {
        cleanUpAfterClip()
        releaseMediaPlayer()
    }

    public override fun onCleared() {
        background()
    }

    private fun playNext(playlist: Queue<Clip>) {
        val nextClip = playlist.poll()
        if (nextClip != null) {
            if (!isCurrentPlayingClip(nextClip.clipID, currentlyPlaying.value)) {
                try {
                    loadNewClip(nextClip.uRI)
                } catch (ignored: IOException) {
                    error.value = PlaybackFailedException(nextClip.uRI, getExceptionMsg(nextClip.uRI))
                    playNext(playlist)
                    return
                }
            }
            mediaPlayer.seekTo(getPositionForClip(nextClip.clipID).value!!)
            mediaPlayer.start()
            currentlyPlaying.value = CurrentlyPlaying(
                Clip(nextClip.clipID, nextClip.uRI),
                false,
                playlist
            )

            schedulePositionUpdates()
        }
    }

    private fun getExceptionMsg(uri: String): Int {
        return if (File(uri).exists()) 1 else 0
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        val wasPlaying = cleanUpAfterClip()
        if (wasPlaying != null) {
            if (!wasPlaying.playlist.isEmpty()) {
                playNext(wasPlaying.playlist)
            }
        }
    }

    private fun cleanUpAfterClip(): CurrentlyPlaying? {
        val wasPlaying = currentlyPlaying.value
        cancelPositionUpdates()
        currentlyPlaying.value = null
        if (wasPlaying != null) {
            getPositionForClip(wasPlaying.clipID).value = 0
        }
        return wasPlaying
    }

    private fun getPositionForClip(clipID: String): MutableLiveData<Int> {
        val liveData: MutableLiveData<Int>
        if (positions.containsKey(clipID)) {
            liveData = positions[clipID]!!
        } else {
            liveData = MutableLiveData()
            liveData.value = 0
            positions[clipID] = liveData
        }
        return liveData
    }

    fun getError(): LiveData<Exception?> {
        return error
    }

    fun errorDisplayed() {
        error.value = null
    }

    private fun schedulePositionUpdates() {
        positionUpdatesCancellable = scheduler.repeat(
            Runnable {
                val currentlyPlaying = currentlyPlaying.value
                if (currentlyPlaying != null) {
                    val position = getPositionForClip(currentlyPlaying.clip.clipID)
                    position.postValue(mediaPlayer.currentPosition)
                }
            },
            1000 / 12
        ) // 12fps
    }

    private fun cancelPositionUpdates() {
        positionUpdatesCancellable?.cancel()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer.release()
        _mediaPlayer = null
    }

    private fun setupNewMediaPlayer(): MediaPlayer {
        val newMediaPlayer: MediaPlayer = mediaPlayerFactory.get()
        newMediaPlayer.setOnCompletionListener(this)
        return newMediaPlayer
    }

    private fun isCurrentPlayingClip(clipID: String, currentlyPlayingValue: CurrentlyPlaying?): Boolean {
        return currentlyPlayingValue != null && currentlyPlayingValue.clip.clipID == clipID
    }

    @Throws(IOException::class)
    private fun loadNewClip(uri: String) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(uri)
        mediaPlayer.prepare()
    }

    private class CurrentlyPlaying internal constructor(val clip: Clip, val isPaused: Boolean, val playlist: Queue<Clip>) {

        val clipID: String
            get() = clip.clipID

        fun paused(): CurrentlyPlaying {
            return CurrentlyPlaying(clip, true, playlist)
        }
    }

    class Factory(private val mediaPlayerFactory: Supplier<MediaPlayer>, private val scheduler: Scheduler) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioClipViewModel(mediaPlayerFactory, scheduler) as T
        }
    }
}

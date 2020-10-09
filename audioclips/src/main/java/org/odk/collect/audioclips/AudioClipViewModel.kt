package org.odk.collect.audioclips

import android.media.MediaPlayer
import androidx.lifecycle.*
import org.odk.collect.async.Cancellable
import org.odk.collect.async.Scheduler
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Supplier
import kotlin.jvm.Throws

class AudioClipViewModel internal constructor(private val mediaPlayerFactory: Supplier<MediaPlayer>, private val scheduler: Scheduler) : ViewModel(), MediaPlayer.OnCompletionListener {

    private var mediaPlayer: MediaPlayer? = null
    private val currentlyPlaying = MutableLiveData<CurrentlyPlaying?>(null)
    private val error = MutableLiveData<Exception?>()
    private val positions: MutableMap<String, MutableLiveData<Int>?> = HashMap()
    private var positionUpdatesCancellable: Cancellable? = null

    fun play(clip: Clip) {
        val playlist = LinkedList<Clip>()
        playlist.add(clip)
        playNext(playlist)
    }

    fun playInOrder(clips: List<Clip>?) {
        val playlist: Queue<Clip> = LinkedList(clips)
        playNext(playlist)
    }

    fun stop() {
        if (currentlyPlaying.value != null) {
            getMediaPlayer()!!.stop()
        }
        cleanUpAfterClip()
    }

    fun pause() {
        getMediaPlayer()!!.pause()
        val currentlyPlayingValue = currentlyPlaying.value
        if (currentlyPlayingValue != null) {
            currentlyPlaying.value = currentlyPlayingValue.paused()
        }
    }

    fun isPlaying(clipID: String): LiveData<Boolean> {
        return Transformations.map(currentlyPlaying) { value: CurrentlyPlaying? ->
            if (isCurrentPlayingClip(clipID, value)) {
                return@map !value!!.isPaused
            } else {
                return@map false
            }
        }
    }

    fun getPosition(clipID: String): LiveData<Int> {
        return getPositionForClip(clipID)
    }

    fun setPosition(clipID: String, newPosition: Int) {
        if (isCurrentPlayingClip(clipID, currentlyPlaying.value)) {
            getMediaPlayer()!!.seekTo(newPosition)
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
            getMediaPlayer()!!.seekTo(getPositionForClip(nextClip.clipID).value!!)
            getMediaPlayer()!!.start()
            currentlyPlaying.value = CurrentlyPlaying(
                    Clip(nextClip.clipID, nextClip.uRI),
                    false,
                    playlist)
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
        positionUpdatesCancellable = scheduler.repeat(Runnable {
            val currentlyPlaying = currentlyPlaying.value
            if (currentlyPlaying != null) {
                val position = getPositionForClip(currentlyPlaying.clip.clipID)
                position.postValue(getMediaPlayer()!!.currentPosition)
            }
        }, 500)
    }

    private fun cancelPositionUpdates() {
        if (positionUpdatesCancellable != null) {
            positionUpdatesCancellable!!.cancel()
        }
    }

    private fun releaseMediaPlayer() {
        getMediaPlayer()!!.release()
        mediaPlayer = null
    }

    private fun getMediaPlayer(): MediaPlayer? {
        if (mediaPlayer == null) {
            mediaPlayer = mediaPlayerFactory.get()
            mediaPlayer!!.setOnCompletionListener(this)
        }
        return mediaPlayer
    }

    private fun isCurrentPlayingClip(clipID: String, currentlyPlayingValue: CurrentlyPlaying?): Boolean {
        return currentlyPlayingValue != null && currentlyPlayingValue.clip.clipID == clipID
    }

    @Throws(IOException::class)
    private fun loadNewClip(uri: String) {
        getMediaPlayer()!!.reset()
        getMediaPlayer()!!.setDataSource(uri)
        getMediaPlayer()!!.prepare()
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
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AudioClipViewModel(mediaPlayerFactory, scheduler) as T
        }
    }
}
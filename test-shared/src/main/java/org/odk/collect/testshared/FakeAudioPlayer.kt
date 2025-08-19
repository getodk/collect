package org.odk.collect.testshared

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.audioclips.AudioPlayer
import org.odk.collect.audioclips.AudioPlayerFactory
import org.odk.collect.audioclips.Clip
import java.util.function.Consumer

class FakeAudioPlayer : AudioPlayer {
    private val playingChangedListeners: MutableMap<String, Consumer<Boolean>> = HashMap()
    private val positionChangedListeners: MutableMap<String, Consumer<Int>> = HashMap()
    private val positions: MutableMap<String, Int> = HashMap()

    var playedClips: Int = 0
        private set
    var isPaused: Boolean = false
        private set
    var currentClip: Clip? = null
        private set

    override fun isLoading(): LiveData<Boolean> {
        return MutableLiveData(false)
    }

    override fun play(clip: Clip) {
        this.currentClip = clip
        playedClips++
        isPaused = false
        playingChangedListeners[clip.clipID]!!.accept(true)
    }

    override fun pause() {
        isPaused = true
        playingChangedListeners[currentClip!!.clipID]!!.accept(false)
    }

    override fun setPosition(clipId: String, position: Int) {
        positions[clipId] = position
        positionChangedListeners[clipId]!!.accept(position)
    }

    override fun onPlayingChanged(clipID: String, playingConsumer: Consumer<Boolean>) {
        playingChangedListeners[clipID] = playingConsumer
    }

    override fun onPositionChanged(clipID: String, positionConsumer: Consumer<Int>) {
        positionChangedListeners[clipID] = positionConsumer
    }

    override fun onPlaybackError(error: Consumer<Exception>) {
    }

    override fun stop() {
        currentClip?.also {
            playingChangedListeners[it.clipID]?.accept(false)
        }

        currentClip = null
    }

    override fun playInOrder(clips: List<Clip>) {
        playedClips += clips.size
    }

    fun getPosition(clipId: String): Int? {
        return positions[clipId]
    }
}

class FakeAudioPlayerFactory : AudioPlayerFactory {
    lateinit var audioPlayer: FakeAudioPlayer
        private set

    override fun create(
        activity: ComponentActivity,
        lifecycleOwner: LifecycleOwner
    ): AudioPlayer {
        return FakeAudioPlayer().also {
            audioPlayer = it
        }
    }
}

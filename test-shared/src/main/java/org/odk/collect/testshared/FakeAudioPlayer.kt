package org.odk.collect.testshared

import androidx.lifecycle.LifecycleOwner
import org.odk.collect.audioclips.AudioClipViewModel
import org.odk.collect.audioclips.AudioPlayer
import org.odk.collect.audioclips.AudioPlayerFactory
import org.odk.collect.audioclips.Clip
import java.util.function.Consumer

class FakeAudioPlayer : AudioPlayer {
    private val playingChangedListeners: MutableMap<String, Consumer<Boolean>> = HashMap()
    private val positionChangedListeners: MutableMap<String, Consumer<Int>> = HashMap()
    private val positions: MutableMap<String, Int> = HashMap()

    var playInOrderCount: Int = 0
        private set
    var isPaused: Boolean = false
        private set
    var currentClip: Clip? = null
        private set

    override fun play(clip: Clip) {
        this.currentClip = clip
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
        playInOrderCount++
    }

    fun getPosition(clipId: String): Int? {
        return positions[clipId]
    }
}

class FakeAudioPlayerFactory : AudioPlayerFactory {
    lateinit var audioPlayer: FakeAudioPlayer
        private set

    override fun create(
        viewModel: AudioClipViewModel,
        lifecycleOwner: LifecycleOwner
    ): AudioPlayer {
        return FakeAudioPlayer().also {
            audioPlayer = it
        }
    }
}

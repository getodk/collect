package org.odk.collect.audioclips

import androidx.lifecycle.LiveData
import java.util.function.Consumer

interface AudioPlayer {
    fun isLoading(): LiveData<Boolean>

    fun play(clip: Clip)

    fun pause()

    fun setPosition(clipId: String, position: Int)

    fun onPlayingChanged(clipID: String, playingConsumer: Consumer<Boolean>)

    fun onPositionChanged(clipID: String, positionConsumer: Consumer<Int>)

    fun onPlaybackError(error: Consumer<Exception>)

    fun stop()

    fun playInOrder(clips: List<Clip>)
}

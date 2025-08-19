package org.odk.collect.android.widgets.utilities

import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.data.consume
import org.odk.collect.async.Scheduler
import org.odk.collect.audioclips.AudioClipViewModel
import org.odk.collect.audioclips.AudioPlayer
import org.odk.collect.audioclips.AudioPlayerFactory
import org.odk.collect.audioclips.Clip
import java.util.function.Consumer

class ViewModelAudioPlayer(
    private val viewModel: AudioClipViewModel,
    private val lifecycleOwner: LifecycleOwner
) : AudioPlayer {
    override fun isLoading(): LiveData<Boolean> {
        return viewModel.isLoading()
    }

    override fun play(clip: Clip) {
        viewModel.play(clip)
    }

    override fun pause() {
        viewModel.pause()
    }

    override fun setPosition(clipId: String, position: Int) {
        viewModel.setPosition(clipId, position)
    }

    override fun onPlayingChanged(clipID: String, playingConsumer: Consumer<Boolean>) {
        viewModel.isPlaying(clipID).observe(lifecycleOwner) {
            playingConsumer.accept(it)
        }
    }

    override fun onPositionChanged(clipID: String, positionConsumer: Consumer<Int>) {
        viewModel.getPosition(clipID).observe(lifecycleOwner) {
            positionConsumer.accept(it)
        }
    }

    override fun onPlaybackError(errorConsumer: Consumer<Exception>) {
        viewModel.getError().consume(lifecycleOwner) { e: Exception ->
            errorConsumer.accept(e)
        }
    }

    override fun stop() {
        viewModel.stop()
    }

    override fun playInOrder(clips: List<Clip>) {
        viewModel.playInOrder(clips)
    }
}

class ViewModelAudioPlayerFactory(private val scheduler: Scheduler) : AudioPlayerFactory {
    override fun create(
        activity: ComponentActivity,
        lifecycleOwner: LifecycleOwner
    ): AudioPlayer {
        val factory = AudioClipViewModel.Factory(::MediaPlayer, scheduler)
        val viewModel = ViewModelProvider(activity, factory)[AudioClipViewModel::class.java]
        return ViewModelAudioPlayer(viewModel, lifecycleOwner)
    }
}

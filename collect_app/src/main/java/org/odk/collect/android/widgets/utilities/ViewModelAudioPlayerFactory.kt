package org.odk.collect.android.widgets.utilities

import androidx.lifecycle.LifecycleOwner
import org.odk.collect.audioclips.AudioClipViewModel
import org.odk.collect.audioclips.AudioPlayer
import org.odk.collect.audioclips.AudioPlayerFactory

class ViewModelAudioPlayerFactory : AudioPlayerFactory {
    override fun create(
        viewModel: AudioClipViewModel,
        lifecycleOwner: LifecycleOwner
    ): AudioPlayer {
        return ViewModelAudioPlayer(viewModel, lifecycleOwner)
    }
}

package org.odk.collect.audioclips

import androidx.lifecycle.LifecycleOwner

interface AudioPlayerFactory {
    fun create(
        viewModel: AudioClipViewModel,
        lifecycleOwner: LifecycleOwner
    ): AudioPlayer
}

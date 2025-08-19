package org.odk.collect.audioclips

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner

interface AudioPlayerFactory {
    fun create(
        activity: ComponentActivity,
        lifecycleOwner: LifecycleOwner
    ): AudioPlayer
}

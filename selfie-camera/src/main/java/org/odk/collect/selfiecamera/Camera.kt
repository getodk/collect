package org.odk.collect.selfiecamera

import android.view.View
import androidx.activity.ComponentActivity
import org.odk.collect.androidshared.livedata.NonNullLiveData

internal interface Camera {
    fun initialize(activity: ComponentActivity, previewView: View)
    fun takePicture(imagePath: String, onImageSaved: () -> Unit, onImageSaveError: () -> Unit)
    fun startVideo(videoPath: String, onVideoSaved: () -> Unit, onVideoSaveError: () -> Unit)
    fun stopVideo()

    fun isRecording(): Boolean
    fun state(): NonNullLiveData<State>

    enum class State {
        UNINITIALIZED,
        INITIALIZED,
        FAILED_TO_INITIALIZE
    }
}

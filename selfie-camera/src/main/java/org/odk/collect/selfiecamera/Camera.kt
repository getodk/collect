package org.odk.collect.selfiecamera

import android.view.View
import androidx.activity.ComponentActivity
import org.odk.collect.androidshared.livedata.NonNullLiveData

internal interface Camera {
    fun initialize(activity: ComponentActivity, previewView: View)

    fun state(): NonNullLiveData<State>

    fun takePicture(imagePath: String, onImageSaved: () -> Unit, onImageSaveError: () -> Unit)

    enum class State {
        UNINITIALIZED,
        INITIALIZED,
        FAILED_TO_INITIALIZE
    }
}

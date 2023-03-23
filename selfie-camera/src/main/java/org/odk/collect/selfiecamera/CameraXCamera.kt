package org.odk.collect.selfiecamera

import android.view.View
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import java.io.File

internal class CameraXCamera : Camera {

    private var activity: ComponentActivity? = null
    private var state = MutableNonNullLiveData(Camera.State.UNINITIALIZED)
    private var imageCapture = ImageCapture.Builder().build()

    override fun initialize(activity: ComponentActivity, previewView: View) {
        this.activity = activity

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider((previewView as PreviewView).surfaceProvider)

                try {
                    cameraProvider.bindToLifecycle(
                        activity,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageCapture
                    )

                    state.value = Camera.State.INITIALIZED
                } catch (e: IllegalArgumentException) {
                    state.value = Camera.State.FAILED_TO_INITIALIZE
                }
            },
            ContextCompat.getMainExecutor(activity)
        )
    }

    override fun state(): NonNullLiveData<Camera.State> {
        return state
    }

    override fun takePicture(
        imagePath: String,
        onImageSaved: () -> Unit,
        onImageSaveError: () -> Unit
    ) {
        Pair(imageCapture, activity).let { (i, a) ->
            if (i == null || a == null) {
                return
            }

            val outputFile = File(imagePath)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            i.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(a),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException) {
                        onImageSaveError()
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        onImageSaved()
                    }
                }
            )
        }
    }
}

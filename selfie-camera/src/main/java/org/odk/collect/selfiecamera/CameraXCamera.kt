package org.odk.collect.selfiecamera

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import java.io.File

internal abstract class CameraXCamera : Camera {

    protected var activity: ComponentActivity? = null
    private var state = MutableNonNullLiveData(Camera.State.UNINITIALIZED)

    protected abstract fun getUseCase(): UseCase

    override fun initialize(activity: ComponentActivity, previewView: View) {
        this.activity = activity

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider((previewView as PreviewView).surfaceProvider)

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                try {
                    cameraProviderFuture.get().bindToLifecycle(
                        activity,
                        cameraSelector,
                        preview,
                        getUseCase()
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
}

internal class CameraXStillCamera : CameraXCamera(), StillCamera {

    private var imageCapture = ImageCapture.Builder().build()

    override fun getUseCase(): UseCase {
        return imageCapture
    }

    override fun takePicture(
        imagePath: String,
        onImageSaved: () -> Unit,
        onImageSaveError: () -> Unit,
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

internal class CameraXVideoCamera : CameraXCamera(), VideoCamera {

    private val videoCapture by lazy {
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(activity!!))
            .build()

        VideoCapture.withOutput(recorder)
    }

    private var recording: Recording? = null

    override fun getUseCase(): UseCase {
        return videoCapture
    }

    @SuppressLint("MissingPermission")
    override fun startVideo(
        videoPath: String,
        onVideoSaved: () -> Unit,
        onVideoSaveError: () -> Unit,
    ) {
        Pair(videoCapture, activity).let { (v, a) ->
            if (v == null || a == null) {
                return
            }

            val outputFile = File(videoPath)
            val outputFileOptions = FileOutputOptions.Builder(outputFile).build()

            recording = v.output
                .prepareRecording(a, outputFileOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(a)) { event ->
                    if (event is VideoRecordEvent.Finalize) {
                        if (event.hasError()) {
                            onVideoSaveError()
                        } else {
                            onVideoSaved()
                        }
                    }
                }
        }
    }

    override fun stopVideo() {
        recording?.stop()
    }

    override fun isRecording(): Boolean {
        return recording != null
    }
}

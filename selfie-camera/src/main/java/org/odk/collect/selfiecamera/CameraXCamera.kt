package org.odk.collect.selfiecamera

import android.annotation.SuppressLint
import android.view.View
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File

class CameraXCamera : Camera {

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activity: ComponentActivity? = null

    private var recording: Recording? = null

    override fun initialize(activity: ComponentActivity, previewView: View) {
        this.activity = activity

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider((previewView as PreviewView).surfaceProvider)

                val recorder = Recorder.Builder()
                    .setExecutor(ContextCompat.getMainExecutor(activity))
                    .build()

                imageCapture = ImageCapture.Builder().build()
                videoCapture = VideoCapture.withOutput(recorder)

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProviderFuture.get()
                    .bindToLifecycle(activity, cameraSelector, preview, imageCapture, videoCapture)
            },
            ContextCompat.getMainExecutor(activity)
        )
    }

    override fun takePicture(
        imagePath: String,
        onImageSaved: () -> Unit,
        onImageSaveError: () -> Unit,
    ) {
        val outputFile = File(imagePath)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture!!.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(activity!!),
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

    @SuppressLint("MissingPermission")
    override fun startVideo(
        videoPath: String,
        onVideoSaved: () -> Unit,
        onVideoSaveError: () -> Unit,
    ) {
        val outputFile = File(videoPath)
        val outputFileOptions = FileOutputOptions.Builder(outputFile).build()

        recording = videoCapture!!.output
            .prepareRecording(activity!!, outputFileOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(activity!!)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    if (event.hasError()) {
                        onVideoSaveError()
                    } else {
                        onVideoSaved()
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

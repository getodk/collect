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
import java.io.File

class CameraXCamera : Camera {

    private var imageCapture: ImageCapture? = null
    private var activity: ComponentActivity? = null

    override fun initialize(activity: ComponentActivity, previewView: View) {
        this.activity = activity

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider((previewView as PreviewView).surfaceProvider)

                imageCapture = ImageCapture.Builder()
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProviderFuture.get()
                    .bindToLifecycle(activity, cameraSelector, preview, imageCapture)
            },
            ContextCompat.getMainExecutor(activity)
        )
    }

    override fun takePicture(imagePath: String, onImageSaved: () -> Unit, onImageSaveError: () -> Unit) {
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
}

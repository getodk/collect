/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.selfiecamera

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.shared.injection.ObjectProviderHost
import org.odk.collect.strings.localization.LocalizedActivity
import java.io.File

class CaptureSelfieActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!permissionsGranted()) {
            finish()
            return
        }

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        setContentView(R.layout.activity_capture_selfie)

        if (intent.getBooleanExtra(EXTRA_VIDEO, false)) {
            val cameraProvider = ProcessCameraProvider.getInstance(this)
            cameraProvider.addListener(
                {
                    setupVideo(cameraProvider.get())
                },
                ContextCompat.getMainExecutor(this)
            )
        } else {
            val camera = Camera()

            val previewView = findViewById<View>(R.id.preview)
            camera.initialize(this, previewView)

            val imagePath = intent.getStringExtra(EXTRA_TMP_PATH) + "/tmp.jpg"
            previewView.setOnClickListener {
                camera.takePicture(
                    imagePath,
                    { ExternalAppUtils.returnSingleValue(this, imagePath) },
                    {}
                )
            }

            showLongToast(this, R.string.take_picture_instruction)
        }
    }

    private fun permissionsGranted(): Boolean {
        val objectProvider = (application as ObjectProviderHost).getObjectProvider()
        val permissionsChecker = objectProvider.provide(PermissionsChecker::class.java)

        return if (intent.getBooleanExtra(EXTRA_VIDEO, false)) {
            permissionsChecker.isPermissionGranted(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        } else {
            permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)
        }
    }

    @SuppressLint("MissingPermission") // Checked on Activity launch
    private fun setupVideo(cameraProvider: ProcessCameraProvider) {
        val previewView = findViewById<PreviewView>(R.id.preview)

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(this))
            .build()

        val videoCapture = VideoCapture.withOutput(recorder)

        val outputFile = File(intent.getStringExtra(EXTRA_TMP_PATH), "tmp.mp4")
        val outputFileOptions = FileOutputOptions.Builder(outputFile).build()

        var recording: Recording? = null
        previewView.setOnClickListener {
            recording.let {
                if (it == null) {
                    recording = videoCapture.output
                        .prepareRecording(this, outputFileOptions)
                        .withAudioEnabled()
                        .start(ContextCompat.getMainExecutor(this)) { event ->
                            if (event is VideoRecordEvent.Finalize) {
                                ExternalAppUtils.returnSingleValue(this, outputFile.absolutePath)
                            }
                        }

                    showLongToast(this, getString(R.string.stop_video_capture_instruction))
                } else {
                    it.stop()
                }
            }
        }

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
        showLongToast(this, getString(R.string.start_video_capture_instruction))
    }

    companion object {
        const val EXTRA_TMP_PATH = "tmpPath"
        const val EXTRA_VIDEO = "video"
    }
}

private class Camera {

    private var imageCapture: ImageCapture? = null
    private var activity: ComponentActivity? = null

    fun initialize(activity: ComponentActivity, previewView: View) {
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

    fun takePicture(imagePath: String, onImageSaved: () -> Unit, onImageSaveError: () -> Unit) {
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

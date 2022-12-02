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
import org.odk.collect.permissions.ContextCompatPermissionChecker
import org.odk.collect.strings.localization.LocalizedActivity
import java.io.File

class CaptureSelfieActivity : LocalizedActivity() {

    private var recording: Recording? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
            !ContextCompatPermissionChecker(this).isPermissionGranted(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            finish()
            return
        }

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        setContentView(R.layout.activity_capture_selfie)

        val cameraProvider = ProcessCameraProvider.getInstance(this)
        cameraProvider.addListener(
            {
                if (intent.getBooleanExtra(EXTRA_VIDEO, false)) {
                    setupVideo(cameraProvider.get())
                } else {
                    setupStillImage(cameraProvider.get())
                }
            },
            ContextCompat.getMainExecutor(this)
        )
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

    private fun setupStillImage(cameraProvider: ProcessCameraProvider) {
        val previewView = findViewById<PreviewView>(R.id.preview)

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(previewView.display.rotation)
            .build()

        val outputFile = File(intent.getStringExtra(EXTRA_TMP_PATH), "tmp.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        previewView.setOnClickListener {
            imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(error: ImageCaptureException) {
                        // Ignored
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        ExternalAppUtils.returnSingleValue(
                            this@CaptureSelfieActivity,
                            outputFile.absolutePath
                        )
                    }
                }
            )
        }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        showLongToast(this, R.string.take_picture_instruction)
    }

    companion object {
        const val EXTRA_TMP_PATH = "tmpPath"
        const val EXTRA_VIDEO = "video"
    }
}

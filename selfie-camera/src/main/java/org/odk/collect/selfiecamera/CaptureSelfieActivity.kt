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
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.externalapp.ExternalAppUtils
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.shared.injection.ObjectProvider
import org.odk.collect.shared.injection.ObjectProviderHost
import org.odk.collect.strings.localization.LocalizedActivity

class CaptureSelfieActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val objectProvider = (application as ObjectProviderHost).getObjectProvider()

        if (!permissionsGranted(objectProvider)) {
            finish()
            return
        }

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
        setContentView(R.layout.activity_capture_selfie)

        val previewView = findViewById<View>(R.id.preview)
        val camera = objectProvider.provide(Camera::class.java)
        camera.initialize(this, previewView)

        if (intent.getBooleanExtra(EXTRA_VIDEO, false)) {
            val videoPath = intent.getStringExtra(EXTRA_TMP_PATH) + "/tmp.mp4"
            previewView.setOnClickListener {
                if (!camera.isRecording()) {
                    camera.startVideo(
                        videoPath,
                        { ExternalAppUtils.returnSingleValue(this, videoPath) },
                        { ToastUtils.showShortToast(this, R.string.camera_error) }
                    )

                    showLongToast(this, getString(R.string.stop_video_capture_instruction))
                } else {
                    camera.stopVideo()
                }
            }

            showLongToast(this, getString(R.string.start_video_capture_instruction))
        } else {
            val imagePath = intent.getStringExtra(EXTRA_TMP_PATH) + "/tmp.jpg"
            previewView.setOnClickListener {
                camera.takePicture(
                    imagePath,
                    { ExternalAppUtils.returnSingleValue(this, imagePath) },
                    { ToastUtils.showShortToast(this, R.string.camera_error) }
                )
            }

            showLongToast(this, R.string.take_picture_instruction)
        }
    }

    private fun permissionsGranted(objectProvider: ObjectProvider): Boolean {
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

    companion object {
        const val EXTRA_TMP_PATH = "tmpPath"
        const val EXTRA_VIDEO = "video"
    }
}

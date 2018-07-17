package org.odk.collect.android.activities;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CameraUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.views.CameraPreview;
import org.odk.collect.android.widgets.VideoWidget;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/*https://developer.android.com/guide/topics/media/camera.html#capture-video*/

public class CaptureSelfieVideoActivity extends Activity {
    private Camera camera;
    private CameraPreview camPreview;
    private int cameraId;
    private boolean recording;
    private MediaRecorder mediaRecorder;
    private String outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager
                .LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture_selfie);
        FrameLayout preview = findViewById(R.id.camera_preview);

        try {
            cameraId = CameraUtils.getFrontCameraId();
            camera = CameraUtils.getCameraInstance(this, cameraId);
        } catch (Exception e) {
            Timber.e(e);
        }

        this.camPreview = new CameraPreview(this, camera);
        preview.addView(this.camPreview);

        mediaRecorder = new MediaRecorder();

        this.camPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Collect.allowClick()) {
                    if (!recording) {
                        // initialize video camera
                        if (prepareVideoRecorder()) {
                            // Camera is available and unlocked, MediaRecorder is prepared,
                            // now you can start recording
                            mediaRecorder.start();
                            Timber.d("Started recording");

                            // inform the user that recording has started
                            ToastUtils.showLongToast(getString(R.string.stop_video_capture_instruction));
                            recording = true;
                        } else {
                            // prepare didn't work, release the camera
                            releaseMediaRecorder();
                        }
                    } else {
                        try {
                            camPreview.setClickable(false);
                            Timber.d("About to stop recording");
                            mediaRecorder.stop();  // stop the recording

                            releaseMediaRecorder(); // release the MediaRecorder object
                            camera.lock();         // take camera access back from MediaRecorder
                            recording = false;
                            releaseCamera();

                            Intent i = new Intent();
                            i.setData(Uri.fromFile(new File(outputFile)));
                            setResult(RESULT_OK, i);
                        } catch (RuntimeException e) {
                            // RuntimeException is thrown when stop() is called immediately after start().
                            // In this case the output file is not properly constructed ans should be deleted.
                            Timber.d("RuntimeException: stop() is called immediately after start()");
                            //noinspection ResultOfMethodCallIgnored
                            new File(outputFile).delete();

                            releaseMediaRecorder(); // release the MediaRecorder object
                            camera.lock();         // take camera access back from MediaRecorder
                            recording = false;
                            releaseCamera();
                        }
                        finish();
                    }
                }
            }
        });

        ToastUtils.showLongToast(getString(R.string.start_video_capture_instruction));
    }

    private boolean prepareVideoRecorder() {

        mediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mediaRecorder.setProfile(CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        outputFile = VideoWidget.getOutputMediaFile(VideoWidget.MEDIA_TYPE_VIDEO).toString();
        mediaRecorder.setOutputFile(outputFile);

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(camPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Timber.e(e);
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            // release the camera for other applications
            try {
                camera.release();
                Timber.i("Camera released");
            } catch (Exception e) {
                Timber.d("Camera has been already released");
            }
            camera = null;
        }
    }

    @Override
    protected void onPause() {
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (camera == null) {
            setContentView(R.layout.activity_capture_selfie);
            FrameLayout preview = findViewById(R.id.camera_preview);

            try {
                cameraId = CameraUtils.getFrontCameraId();
                camera = CameraUtils.getCameraInstance(this, cameraId);
            } catch (Exception e) {
                Timber.e(e);
            }

            this.camPreview = new CameraPreview(this, camera);
            preview.addView(this.camPreview);
        }
    }
}

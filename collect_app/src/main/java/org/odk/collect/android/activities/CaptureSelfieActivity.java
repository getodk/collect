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

package org.odk.collect.android.activities;

import android.hardware.Camera;
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

import timber.log.Timber;

public class CaptureSelfieActivity extends CollectAbstractActivity {
    private Camera camera;
    private CameraPreview preview;
    private int cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager
                .LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture_selfie);
        FrameLayout previewLayout = findViewById(R.id.camera_preview);

        try {
            cameraId = CameraUtils.getFrontCameraId();
            camera = CameraUtils.getCameraInstance(this, cameraId);
        } catch (Exception e) {
            Timber.e(e);
        }

        preview = new CameraPreview(this, camera);
        previewLayout.addView(preview);

        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview.setEnabled(false);
                camera.takePicture(null, null, picture);
            }
        });

        ToastUtils.showLongToast(R.string.take_picture_instruction);
    }

    private final Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            CameraUtils.savePhoto(Collect.TMPFILE_PATH, data);
            setResult(RESULT_OK);
            finish();
        }
    };


    @Override
    protected void onPause() {
        camera = null;
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

            this.preview = new CameraPreview(this, camera);
            preview.addView(this.preview);
            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    camera.takePicture(null, null, picture);
                }
            });
        }
    }

    public static boolean isFrontCameraAvailable() {
        //https://developer.android.com/guide/topics/media/camera.html#check-camera-features
        for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camNo, camInfo);
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false; // No front-facing camera found
    }
}
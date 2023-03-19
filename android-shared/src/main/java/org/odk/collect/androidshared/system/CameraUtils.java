package org.odk.collect.androidshared.system;

/*
Copyright 2018 Theodoros Tyrovouzis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import timber.log.Timber;

public class CameraUtils {
    public static int getFrontCameraId() {
        for (int camNo = 0; camNo < Camera.getNumberOfCameras(); camNo++) {
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(camNo, camInfo);

            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camNo;
            }
        }
        Timber.w("No Available front camera");
        return -1;
    }

    public boolean isFrontCameraAvailable(Context context) {
        try {
            //https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics.html
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                String[] cameraId = cameraManager.getCameraIdList();
                for (String id : cameraId) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return true;
                    }
                }
            }
        } catch (CameraAccessException | NullPointerException e) {
            Timber.e(e);
        }
        return false; // No front-facing camera found
    }
}
